
# necessary-evil

*necessary-evil* is an implementation of [XML-RPC](http://xml-rpc.com/)
built on top of the [ring http
library](https://github.com/mmcgrana/ring) for Clojure. XML-RPC is a
bit nasty, but it is the basis of a number of other standards such as
certain blogging APIs and Ping Back.

## Usage


    (require '[necessary-evil.core :as xml-rpc])

Making a client request is very simple:

    (xml-rpc/rpc-call "http://example.com/rpc" :system.listMethods) 

This will either return a clojure data structure (for example you
might expect a vector of strings here if this was the python implement
here). If there is a fault, a `necessary-evil.methodresponse.Fault`
record will be returned in its place.

### xml-rpc mappings

This table describes the mapping of clojure datastructures and types
to XML-RPC types

<table style="width: 100%">
    <thead>
    <tr><th>XML-RPC Element</th><th>Clojure or Java type</th></tr>
    </thead>
    <tbody>
        <tr><td>array</td><td>clojure.lang.IPersistentVector</td></tr>
        <tr><td>base64</td><td>byte-array</td></tr>
        <tr><td>boolean</td><td>java.lang.Boolean</td></tr>
        <tr><td>dateTime.iso8601</td><td>org.joda.time.DateTime</td></tr>
        <tr><td>double</td><td>java.lang.Double</td></tr>
        <tr><td>i4</td><td>java.lang.Integer</td></tr>
        <tr><td>int</td><td>java.lang.Integer</td></tr>
        <tr><td>struct</td><td>clojure.lang.IPersistantMap — <em>clojure.lang.Keyword keys</em></td></tr>
        <tr><td><em>no element</em></td><td>java.lang.String</td></tr>
    </tbody>
</table>

## Installation

FIXME: write

## License

Copyright (C) 2010 Andrew Brehaut

Distributed under the Eclipse Public License, the same as Clojure.
