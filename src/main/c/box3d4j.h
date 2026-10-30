#pragma once

#include <stdint.h>
#include <box3d/box3d.h>

static inline uintptr_t b3World_GetBodyMoveEvents(b3WorldId worldId, int* moveCount)
{
	b3BodyEvents events = b3World_GetBodyEvents(worldId);
	*moveCount = events.moveCount;
	return (uintptr_t)events.moveEvents;
}

/// Read a batch of body transforms without allocating native result structs.
/// Writes seven doubles per body in input order: px, py, pz, qx, qy, qz, qw.
/// bodyIds must contain count valid IDs; transforms must have room for 7 * count doubles.
/// Reuse a direct DoubleBuffer or DoublePointer to avoid array copying and per-field JNI calls.
/// Buffer/pointer positions are honored and are not advanced. count must be nonnegative.
/// A zero count does not access either pointer. Do not mutate or step the worlds concurrently.
static inline void b3Body_GetTransformsPacked(const b3BodyId* bodyIds, double* transforms, int count)
{
	for (int i = 0; i < count; ++i)
	{
		b3WorldTransform transform = b3Body_GetTransform(bodyIds[i]);
		transforms[0] = transform.p.x;
		transforms[1] = transform.p.y;
		transforms[2] = transform.p.z;
		transforms[3] = transform.q.v.x;
		transforms[4] = transform.q.v.y;
		transforms[5] = transform.q.v.z;
		transforms[6] = transform.q.s;
		transforms += 7;
	}
}

/// Copy the current step's move events into caller-owned buffers without native allocations.
/// Returns the total event count and copies at most capacity events, without consuming them.
/// Pass capacity = 0 and null outputs to query the required size. capacity must be nonnegative.
/// Provide capacity uint64_t body IDs, 7 * capacity doubles, and capacity sleep-flag bytes.
/// IDs use b3StoreBodyId encoding. Transforms are px, py, pz, qx, qy, qz, qw; flags are 0 or 1.
/// Buffer/pointer positions are honored and are not advanced. Do not mutate or step the world concurrently.
static inline int b3World_CopyBodyMoveEventsPacked(b3WorldId worldId, uint64_t* bodyIds,
	double* transforms, uint8_t* fellAsleep, int capacity)
{
	b3BodyEvents events = b3World_GetBodyEvents(worldId);
	int count = events.moveCount < capacity ? events.moveCount : capacity;
	for (int i = 0; i < count; ++i)
	{
		const b3BodyMoveEvent* event = events.moveEvents + i;
		bodyIds[i] = b3StoreBodyId(event->bodyId);
		transforms[0] = event->transform.p.x;
		transforms[1] = event->transform.p.y;
		transforms[2] = event->transform.p.z;
		transforms[3] = event->transform.q.v.x;
		transforms[4] = event->transform.q.v.y;
		transforms[5] = event->transform.q.v.z;
		transforms[6] = event->transform.q.s;
		fellAsleep[i] = event->fellAsleep ? 1 : 0;
		transforms += 7;
	}
	return events.moveCount;
}
