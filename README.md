# dates-in-bible

## Description

This repository contains json files of all the explicit dates (day and month) in the Bible and their occurrences within the verses.  
Every occurrence points to a single verse and to the indexes of the words that form the date inside that verse.  
If you find any mistake, please open up a new issue.<br/>
Thanks

## Structure

```
data/dates.csv                 source of truth, reviewed by hand (verse text with <b> tags around the date words)
minified/all.json              every date occurrence in the Bible
minified/{month}/all.json      every occurrence in that month, including verses that mention only the month
minified/{month}/{day}.json    occurrences of that exact day and month
```

Months are numbered from Nisan, as in the Bible itself ("הַחֹדֶשׁ הָרִאשׁוֹן"): 1 = Nisan … 12 = Adar.

## Usage

```
https://raw.githubusercontent.com/elfifo4/dates-in-bible/master/minified/7/10.json
```

Each file has the shape `{"month":7,"day":10,"total":N,"list":[…]}`, where each item in `list` looks like
(Leviticus 16:29 – "בַּחֹדֶשׁ הַשְּׁבִיעִי בֶּעָשׂוֹר לַחֹדֶשׁ"):

```json
{"b":3,"c":16,"v":29,"w":[5,6,7,8]}
```

- `b`, `c`, `v` – book, chapter and verse (1-based)
- `w` – 1-based indexes of the words forming the date, counting words separated by a space or a maqaf

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
