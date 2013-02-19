# occam

[Occam](http://dmm.sysc.pdx.edu) is a program designed to facilitate [Reconstructability Analysis](http://www.sysc.pdx.edu/download/papers/ldlpitfabstract.htm) of arbitrary data sets.

This library manipulates Occam input files and data to various ends.  Need to set aside random portions of your data for testing?  Need to perform some custom binning for your continuous variable?  Need to make arbitrary transformations to your data?  You have come to the right place.

## Usage

Add the occam library to your project.clj:

```clj
[occam "0.0.2"]
```

Require the library and transform some data:

```clj
(require '[occam.core :as occam])
(occam/transform-occam 
 #(occam/parcel-test-data % 0.3) 
 "path/to/unprocessed/occam/file" "path/to/output/file")
```

## License

Copyright © 2012 Ryan Spangler

Distributed under the Eclipse Public License, the same as Clojure.
