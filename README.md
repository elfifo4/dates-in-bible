# dates-in-bible

## Description

This repository contains json files of all the explicit dates (day and month) in the Bible and their occurrences within the verses.  
Every occurrence points to a single verse and to the indexes of the words that form the date inside that verse.  
If you find any mistake, please open up a new issue.<br/>
Thanks

## What counts as a date

- A **day and a month**, sometimes with a year (e.g. "בַּחֹדֶשׁ הַשְּׁבִיעִי בֶּעָשׂוֹר לַחֹדֶשׁ").
- Dates of the festivals in the laws of the Torah are included.
- Mentions of a year alone, a month alone, durations and ages are **not** included.
- Every occurrence belongs to exactly one verse. A verse with several dates appears once per date.
- When the verse states only the day and the month is known from the context ("בּוֹ", "לַחֹדֶשׁ הַזֶּה"),
  the verse is still included, and only the words that appear in that verse are highlighted.
- The highlighted words are the day and the month, and the year when it is part of the same phrase
  (up to the king's name or other reference point, without titles such as "מֶלֶךְ בָּבֶל").

## Structure

```
data/dates.csv                 source of truth — edit this file (verse text with <b> tags around the date words)
minified/all.json              every date in the Bible
minified/{month}/all.json      every date in that month
minified/{month}/{day}.json    every occurrence of that exact day and month
generator/                     Kotlin program that validates data/dates.csv and regenerates minified/
```

Months are numbered from Nisan, as in the Bible itself ("הַחֹדֶשׁ הָרִאשׁוֹן"):

| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| ניסן | אייר | סיון | תמוז | אב | אלול | תשרי | חשוון | כסלו | טבת | שבט | אדר |

The months of the Flood (Genesis 7–8) follow the same counting; `data/dates.csv` notes that some
commentators count them from Tishrei.

## Usage

```
https://raw.githubusercontent.com/elfifo4/dates-in-bible/master/minified/7/10.json
```

```json
{"month":7,"day":10,"total":4,"diff_verses":4,"list":[{"b":2,"c":16,"v":29,"w":[5,6,7,8]},…]}
```

- `b` – book index, **0-based** in the order of the Hebrew Bible (0 = Genesis … 38 = 2 Chronicles), as in roots-in-bible
- `c`, `v` – chapter and verse, 1-based
- `w` – 1-based indexes of the words forming the date. A word is a run of characters that are neither
  whitespace nor a maqaf. The indexes refer to the verse text **with the qri** (ktiv/qri replaced by the qri).
- `m`, `d` – month and day (only in `all.json` files, where they are not implied by the path)
- `total` – number of occurrences, `diff_verses` – number of different verses

## Editing the data

1. Edit `data/dates.csv` (UTF-8). Keep the `words` column and the `<b>` tags in `text` in sync.
2. Run `./gradlew :generator:run` from the repository root. It checks every row
   (the tags must wrap exactly the words listed in `words`, month and day in range, no duplicates),
   fails on any mistake, and rewrites `minified/`.
3. Commit both `data/dates.csv` and `minified/`.

## License

```
Copyright 2026 Elad Finish

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
