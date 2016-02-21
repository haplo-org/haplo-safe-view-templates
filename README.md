
# Haplo Safe View Templates

HSVT is an HTML templating language with the goal of making it almost impossible to write templates with XSS and other common security issues.

It achieves this through parsing and understanding the structure of HTML, so it knows the context within which inserted values are output. The language is designed to look like HTML with C-style control flow, so it looks familiar to developers.


## Threat model

If a trusted template

 * parses without errors,
 * only uses the built in functions, and
 * does not use `unsafeHTML()` or `unsafeAttributeValue()`,

then all possible input views will write the HTML structure described by the template.

That is, when using a template written by the application author, is it impossible for an attacker controlled view to inject arbitary HTML, such as unwelcome JavaScript.


## License and copyright

Licensed under the MPLv2, (c) Haplo Services 2016.


## Aims

* Terse syntax. Most of the time templates are just HTML tags and inserted and escaped values. Extra syntax, like `{{handlebars}}`, just clutters up the code and makes it hard to read. HSVT inverts that, so that literal text has the special syntax.

* Just interpolating escaped strings doesn't help writing secure applications. A templating language should properly parse HTML and understand the different contexts within it.

* Output really tidy HTML, with the language handling all the edge cases. String interpolation makes this harder than it should be, for example, adding multiple classes to a tag is tricky.

* Whitespace is ignored so you can lay out your templates as clearly as possible. You shouldn't need to chose between legibility and consistent formatting.

* Really good error messages with the exact character position of the error.

* The same templating language for multiple JVM languages. Haplo uses Rhino JavaScript and JRuby, and there's no reason they can't share code.

* The implementation should be easy to embed in a framework. For example, a parsed template can be used over and over again in different threads and in different security contexts.


## Example

Here's an example template:

```
<div class="container">
  each(options) {
    if(indicator) {
      <span class=[
            "indicator"
            switch(indicator) { } primary {"primary"} secondary {"secondary"}
          ]></span>
    }
    <a href=action class=["entry" if(highlight) { "highlight" }]>
      <span class="name"> label </span>
      if(notes) {
        <span> notes </span>
      }
    </a>
  }
</div>
```

This is how the equivalent Handlebars template looks:

```
<div class="container">
  {{#each options}}
    {{#if indicator}}
      <span class="indicator{{_internal__indicator_styles indicator}}"></span>
    {{/if}}
    <a href="{{action}}" class="entry{{#if highlight}} highlight{{/if}}">
      <span class="name">{{label}}</span>
      {{#if notes}}
        <span>{{notes}}</span>
      {{/if}}
    </a>
  {{/each}}
</div>
```

Note this doesn't include the implementation of a `_internal__indicator_styles()` Handlebars helper, which has to output a space prefixed class name if required. This is implemented in JavaScript, which is verbose and means you have to look in another place to understand what's going on.

In HSVT, the `_internal__indicator_styles()` function effectively inlined using the `switch()` builtin. The template will output one or two class names in the class attribute.

While similar in structure, the second template has been properly parsed, and you can't write a template which outputs invalid HTML or doesn't escape things properly. And hopefully it's easier to read.

To try this template, run

```
mvn test
test/try_template.sh examples/choose.hsvt examples/choose_view.json
```

This will output the AST tree, and then render the template with the view.


## Language overview

A template is a whitespace separated list of:

* Values from the view, which are simple bare words. Use `.` as a separator to access nested values. `.` on it's own refers to the current view, eg a value when iterating over a list.

* HTML tags with attributes. These form part of the language, and so are properly parsed and validated. Tag names are lower case only. Tags must be balanced, except for the [void tags defined in the HTML spec](http://www.w3.org/TR/html-markup/syntax.html#syntax-elements) which must not have close tags.

* Literal strings in `" "`, with quotes escaped as `\"`

* Functions, which take an optional anonymous block and zero or more named blocks. (see `if()` and `switch()` in the example above). Blocks can be named with quoted strings using the literal syntax, or just bare words.

* Lists, which are just zero or more of the above in `[ ]`

* URLs, which are explicitly created with a `url(...)` pseudo function, or implicitly inside attributes defined by the HTML specification to contain a URL value. (see below for URL syntax)

* Enclosing view blocks, `^ { }`, the number of `^` characters indicating how many enclosing blocks to traverse, and then the block is rendered with that view.

* C++ style comments, which begin with `//` and end at the end of the line


### Views

The template is rendered from a view. This is a nested data structure, somewhat like JSON but the actual implementation dependent on the Driver. Values in the template refer to named properties in the view.

There is a concept of the "current view". This starts at the root of the data structure. Values are looked up starting from the current view.

`each()` and `within()` change the current view. `each()` changes the view to each element of a list in order, rendering the block for each. `within()` just changes it and renders the block.

Sometimes you need to refer to values which are outside the current view. Use an enclosing view block, for example, to access the 'x' value in the enclosing view, use `^{x}`.


### Missing and wrongly typed values

If a value is missing from the view, or is the wrong type (eg a String when an Array is expected), then nothing will be output.

This is chosen because it is probably better to display the page with missing values than to show an error to the user. A developer mode will alert the developer to these problems, and logging for in production.

Where missing values would have a security implication, the rendering will output something which is safe. For example, empty attributes are omitted from tags, and `<script>` tags with an empty `src` are omitted entirely.


### Attributes on Tags

If your HTML tag is just formed of attributes with constant values (as literals) then your tag is written exactly as it would be in normal HTML files, and will be output exactly as is.

Otherwise, a single value follows, such as `<a href=action>`, and the `action` value will be output, properly escaped.

That single value may be a list, eg `<div class=["name1" otherClass]>`, in which case, a space separated value will be generated. If `otherClass` is missing, just `<div class="name1">` will be output. If the attribute value is a list which evaluates as empty, the attribute will be omitted from the output entirely.

Use the concat() function if you want to generate an attribute value which doesn't have automatic spaces inserted, for example, `<input type="text" name=concat("item" n)>`.

Functions can be used for attribute values.

To specify attributes at runtime, you can use the `*` operator to expand a dictionary into attributes. For example `<div *attrs>` with a view of `{"attrs":{"a","b"}}` would be rendered as `<div a="b">`.


### URLs

URLs are handled specially, because they require different escaping to other values, and URL parameters are generated properly.

URL parsing and escaping is triggered by one of these forms:

* An explicit url pseudo function containing a list `url(...)`

* As an attribute defined in the HTML spec to take a URL value attribute, with a single value, `<a href=url>`

* As an attribute defined in the HTML spec to take a URL value attribute, with a list `<a href=[...]>`

* As any tag attribute with an explict url value, `<a href=url(...)>`

Where a URL is a single value, it is % escaped without escaping reserved characters. This allows values in the view like `/path/to/action` to work as expected, as otherwise characters like '/' would be replaced with % encoded values.

Where a URL is a list, the first value, and all literal values afterwards, are % escaped without escaping reserved characters. Values after the first value are fully % escaped.

The URL list may contain the `?` symbol, which triggers parameter mode. This is designed to do common parameter manipulation in the template language, without lots of troublesome code:

* `key=value` sets the given parameter to the value. This can be a value from the view or a literal. If a parameter with that name is already set, then the value is replaced.

* `*dictionary` retrives value from the view, inteprets it as a dictionary of key value pairs for setting parameters.

* `!key` removes the parameter

Parameters are omitted if the values are not found in the view.

After the (optional) parameters, the URL list may contain the '#' symbol, which triggers URL fragment mode. Any further values are concatenated and used as the URL fragment.

For example, given the definition:

```
<div> <a href=["/action" ? *params !sort kind="person" from=fromDate # "f" index]> label </a> </div>
```

and the view

```
{
  "params": {"q":"query string", "sort":"date"},
  "fromDate": "2015-12-01",
  "index": 42,
  "label": "People"
}
```

the template would be rendered as

```
<div><a href="/action?q=query%20string&kind=person&from=2015-12-01#f42">People</a></div>
```


## Security checking

There are some additional restrictions on templates to encourage secure coding.

* `<script>` tags are not allowed. Use `scriptTag()` instead.

* onX attributes are not allowed, as they contain inline JavaScript. Use id and class attributes to select nodes and add handlers in your client side scripting.

* id, class and style attributes may only contain literal strings, or conditionals which choose between one or more literal strings. This helps stop attackers from being able to manipulate the behaviour of client side scripts. (Use `unsafeAttributeValue()` to override this check if you have appropriate security checking elsewhere.)

* `<style>` tags are not allowed. Use external stylesheets instead.

* if the use of `unsafeHTML()` or `unsafeAttributeValue()` is unavoidable, the value key must have a name beginning with 'unsafe' so the view generation code also contains a hint that it's unsafe.


## Implementation

The prototype has a simple hand-written recursive descent parser which outputs a thread-safe AST. This AST is then rendered with a Driver which provides language specific implentations to retrieve values from the view.

Currently there are three drivers. A JavaScript driver for the Mozilla Rhino interpreter, and two example drivers (neither terribly useful in practise), for nested Java structures, and the JRuby data structures output by JRuby's JSON parser.

All the rendering functions keep track of the context, so escaping and other features is context aware.


### Running the tests

Make sure you have `mvn` and `javac` on your `PATH`, then run

`mvn test`

The tests can be found in the `test/case` directory, and `src/test/ruby/test.rb` is a simple JRuby script to runs them and performs other tests.

`test/try_template.sh` is a script which takes either a template and optional view, or a test case, and dumps the AST and then renders the template.


## Functions

The multiple block syntax allows control flow to be implemented as simple functions. `if()` is just a function which takes a single argument, an anonymous block, and an optional `else` block.

Drivers will be able to implement their own functions, and provide facilities for users to add their own functions for their specific templates (carefully, to make it hard to XSS themselves).

Built in functions are:

### if(value) & unless(value)

A simple conditional control flow function. The Driver determines whether `value` is truthy. The anonymous block is rendered if it is, otherwise the `else` block.

### each(value)

The Driver returns an Iterable of nested views for `value`, then the anonymous block is rendered once per view in that list.

### within(value)

The view is moved to the `value`, then the anonymous block is rendered.

### switch(value)

Evaluate `value` as a string, then render the named block with that name. If that block doesn't exist, render the anonymous block.

### do() { }

Render the anonymous block. `yield:name()` within that block renders the named block to the `do()` function.

This is useful for things like conditionally adding links around generated HTML.

### unsafeHTML(value)

Evaluate `value` as a string, then output directly in the template without escaping. A parse error is thrown if it is used outside the text context (eg can't be used in an attribute value).

The value name, or one of the path elements, must begin with `unsafe` so that it's obvious in the code generating the view that the value will be used unsafely.

### unsafeAttributeValue(value)

When you really have to output a dynamic `class`, `id` or `style`, use this function to override the safety checks.

The value name, or one of the path elements, must begin with `unsafe` so that it's obvious in the code generating the view that the value will be used unsafely.

### template:X()

Include another template in the rendered output, controlled by the Driver. The template name, `X`, is part of the function name.

### yield() & yield:Y()

When rendering a do() anonymous block, or template which has been included in another using the template:X() function, render a block from the calling function. `yield()` renders the anonymous block, `yield:Y()` renders the block named `Y`.

### render(deferred)

Include a deferred render into the current template.

### concat(...)

Outputs the arguments in the current context. Used for turning off the automatic spaces between values in tag attributes.

### url(...)

Pseudo function with special parsing. See URLs section above.

### scriptTag(...)

Pseudo function for generating `<script>` tags. The arguments are a URL, as `url()`, and the resulting URL is output as the `src` attribute in `<script src="..."></script>` tag pair.

Script tags are otherwise not allowed in templates, because including JavaScript is too dangerous and should be prohibited by your Content Security Policy.


## Options

Some HTML parsers don't like tag attributes without quotes. You can include them to keep those parsers happy with the the following directive at the top of your template:

`#option:no-tag-attribute-quote-minimisation`


## TODO

* Destructuring arrays into local variables for compact views & clarity

* Security review, make sure everything escapes properly, and see if there are any more security features which can be implemented

* A proper driver for JRuby which can do more than just JSON data structures

* Linter to find more problems in templates (eg inline JavaScript, improperly escaped URLs)

* Logging of empty and wrongly typed values.

* Internationalisation support


## Contributing

Contributions would be very welcome.

* The most useful thing you can do is try writing some templates in this language and providing feedback. Perhaps you could convert your most complex templates and try running them?

* More test cases for any edge cases you can think of would be wonderful. I'd like to get to 100% test coverage, as this seems appropriate for a security focused project.

* And just as importantly, write bad templates and make sure they throw parse errors and those errors are useful.

* Suggestions on how to write it better, and improvements and fixes.

If you submit a pull request, I'll ask you to confirm you're happy to license your code under the MPLv2 before I merge it.


## Thank you!

[Samir Talwar](http://samirtalwar.com) for feedback and sorting out the build and test systems.

[Alaric Snell-Pym](http://www.snell-pym.org.uk/alaric/), and my colleages at [Haplo Services](http://www.haplo-services.com), for reviewing the language design, giving actionable feedback, and trying out the code.

