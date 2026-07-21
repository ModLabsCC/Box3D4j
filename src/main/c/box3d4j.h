#pragma once

#include <stdint.h>
#include <box3d/box3d.h>

static inline uintptr_t b3World_GetBodyMoveEvents(b3WorldId worldId, int* moveCount)
{
	b3BodyEvents events = b3World_GetBodyEvents(worldId);
	*moveCount = events.moveCount;
	return (uintptr_t)events.moveEvents;
}
