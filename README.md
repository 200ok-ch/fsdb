[![Clojars Project](https://img.shields.io/clojars/v/fsdb.svg)](https://clojars.org/fsdb)

# fsdb

A Clojure library that provides a reasonably convenient database on
top of the file system.


## Idea

The idea behind `fsdb` is simple. One or more files on a file system
make up a database. The files can be of different formats and can be
structured in a directory tree. The names of directories and files as
well as the content of the files make up a database.

Here is an example:

```
% tree example
example
├── people.edn
├── technologies.edn
└── technologies
    └── clojure.yml

$ cat example/people.edn
{:rich {:name "Rich Hickey"}}

$ cat example/technologies.edn
{:clojure {:year "unknown"}}

$ cat example/technologies/clojure.yml
---
year: 2007
```

Reading this structure with `fsdb/read-tree` will result in the
following data structure:

```
{:example
 {:people {:rich {:name "Rich Hickey"}}
  :technologies {:clojure {:year 2007}}}}
```

In this example you can observe multiple aspects of `fsdb`.

* The db is spread of multiple files, these will be merged deeply.
* The files can have different formats.
* Names of directories & files make up the nesting of the resulting data structure.
* Later (more specific entries) overwrite former.

### Metadata

`fsdb` automatically contextualizes the data. Objects in the resulting
data structure have `:path` as well as `:key` set in the metadata.

```
(-> data :example :people :rich meta :path) ;=> [:example :people :rich]
(-> data :example :people :rich meta :key) ;=> :rich
```

## Usage

```
(ns your-ns
  (:require [fsdb]))

(clojure.pprint/pprint (fsdb/read-tree "example"))
```

## License

Copyright © 2017-2021 200ok llc <info@200ok.ch>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
