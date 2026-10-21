---
name: video-find
description: Add videos beside the notions of a Boussole session, the learner's own recordings first, then YouTube, only videos that exist and fit, with the segment that matters. Use when a session would benefit from a visual explanation; optional.
---

# Add videos

A video is optional, and `videos` in `learner.yaml` says how many the learner wants: `none`
means none at all. Add one only where seeing something move or drawn helps more than text:
an algorithm step by step, a protocol exchange, a geometric construction.

## Where to look, in order

1. **The learner's own videos**: lecture recordings or screencasts in the sources folder
   (`boussole sources .` lists them, `.mp4`, `.webm`, `.mov`…). They match the course exactly.
2. **YouTube**, when the sources have no video for this notion.

## Rules

- **Only a recording you have watched the relevant part of, or listened to through its
  transcript**: give the `start` and `end` you checked. If you cannot read the video, ask the
  learner for the minutes, or leave it out.
- **Never write a YouTube ID you have not found in an actual search result or page.** An invented
  or misremembered ID is worse than no video. If you cannot search the web, add no video.
- Prefer, in order: the language of the course, then English; university channels and
  well-known teachers; recent videos; short ones.
- **Point to the segment** that covers the notion with `start` and `end`, so the learner watches
  two to eight minutes, not an hour.
- The video **complements** the notion; the session must stand without it.

## Write it

Inside the notion, after the explanation it illustrates. A recording of the sources folder:

```markdown
::video[Le cours 3, sur le routage]{src=enregistrements/cours3.mp4 start=12:30 end=25:00}
```

A YouTube video:

```markdown
::video[Le routage à vecteur de distance, pas à pas]{youtube=XXXXXXXXXXX start=3:10 end=9:45}
```

The label is a caption in the language of the course. `src` is the path in the sources folder,
as for `::pdf`. The YouTube ID is the 11 characters after `watch?v=` in the video's address.
A recording that is not MP4 or WebM gets `F038`: suggest the learner convert it
(`ffmpeg -i cours3.mkv cours3.mp4`); never convert or move their files yourself.
A recording also counts as a document of the sources: record it in `cursus/sources.yaml`
(`sources-update`).

## Verify

`boussole validate .` checks that every `src` exists in the sources folder (`F031`). For each
YouTube video, check it exists: fetch
`https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=<ID>&format=json`. A `200`
answer with the expected title means it exists; anything else, remove the video. When
`boussole validate --check-videos` is available, run it as well.
