---
name: video-find
description: Add YouTube videos beside the notions of a Boussole session, only videos that exist and fit, with the segment that matters. Use when a session would benefit from a visual explanation; optional.
---

# Add videos

A video is optional. Add one only where seeing something move or drawn helps more than text:
an algorithm step by step, a protocol exchange, a geometric construction.

## Rules

- **Never write a video ID you have not found in an actual search result or page.** An invented
  or misremembered ID is worse than no video. If you cannot search the web, add no video.
- Prefer, in order: the language of the course, then English; university channels and
  well-known teachers; recent videos; short ones.
- **Point to the segment** that covers the notion with `start` and `end`, so the learner watches
  two to eight minutes, not an hour.
- The video **complements** the notion; the session must stand without it.

## Write it

Inside the notion, after the explanation it illustrates:

```markdown
::video[Le routage à vecteur de distance, pas à pas]{youtube=XXXXXXXXXXX start=3:10 end=9:45}
```

The label is a caption in the language of the course. The ID is the 11 characters after
`watch?v=` in the video's address.

## Verify

For each video, check it exists: fetch
`https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=<ID>&format=json`. A `200`
answer with the expected title means it exists; anything else, remove the video. When
`boussole validate --check-videos` is available, run it as well.
