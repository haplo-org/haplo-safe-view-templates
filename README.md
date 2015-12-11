
# A prototype secure web templating language for dynamic JVM languages

A experimental web templating language, with the goal of making it almost impossible to write templates with XSS and other common security issues.

It achieves this through parsing and understanding the structure of HTML, so it knows the context within which inserted values are output. The language is designed to look like HTML with C-style control flow, so it looks familiar to developers.

Licensed under the MPLv2, but with a temporary additional clause to request you don't use it in production until it's done.

## Introduction

There's lots of advice on the internet that you shouldn't write your own templating language. But I have Opinions, and Requirements.

I'm currently using [Handlebars](http://handlebarsjs.com/) in my project, [Haplo](http://haplo.org). While it's pretty good, I would like to use something better.

* It's very noisy. I've noticed that in my applications, most of the time I'm just writing HTML elements and inserting escaped values. The `{{handlebars}}` clutter up your code, and make it hard to read. I'd like to invert that, so that literal text has the special syntax.

* It doesn't help writing secure applications, because it's just interpolating escaped strings. A templating language should properly parse HTML and understand the different contexts within it.

* I'd like to output really tidy HTML, with the language handling all the edge cases. String interpolation makes this harder than it should be, for example, adding multiple classes to an element is tricky.

* I want it to ignore whitespace, as I'd like to lay out my templates as clearly as possible. I shouldn't need to have to chose between legibility and consistent formatting.

* When you mess up your template, I'd like really good error messages with the exact character position of the error.

* I'd like to use the same templating language for multiple JVM languages. I use Rhino JavaScript and JRuby, and there's no reason they can't share code.

* The implementation should be easy to embed in a framework. For example, I should be able to parse a template, then use the parsed template over and over again in different threads and in different security contexts.

This repo contains the start of a templating language which I think will work much better.

I'm publishing it now to get feedback on the language and the implementation.


## Example

Here's a template in this new language:

```
<div class="z__ui_choose_container">
  each(options) {
    if(indicator) {
      <span class=[
            "z__ui_indicator"
            case(indicator) { } primary {"z__ui_indicator_primary"} secondary {"z__ui_indicator_secondary"}
          ]></span>
    }
    <a href=action class=["z__ui_choose_option_entry" if(highlight) { "z__ui_choose_option_entry_highlight" }]>
      <span class="z__ui_choose_option_entry_name"> label </span>
      if(notes) {
        <span> notes </span>
      }
    </a>
  }
</div>
```

This is how the equivalent Handlebars template looks:

```
<div class="z__ui_choose_container">
  {{#each options}}
    {{#if indicator}}
      <span class="z__ui_indicator{{_internal__indicator_styles indicator}}"></span>
    {{/if}}
    <a href="{{action}}" class="z__ui_choose_option_entry{{#if highlight}} z__ui_choose_option_entry_highlight{{/if}}">
      <span class="z__ui_choose_option_entry_name">{{label}}</span>
      {{#if notes}}
        <span>{{notes}}</span>
      {{/if}}
    </a>
  {{/each}}
</div>
```

Note this doesn't include the implementation of a `_internal__indicator_styles()` Handlebars helper, which has to output a space prefixed class name if required. This is implemented in JavaScript, which is verbose and means you have to look in another place to understand what's going on.

In the new templating language, the `_internal__indicator_styles()` function effectively inlined using the `case()` builtin. The template will output one or two class names in the class attribute.

While similar in structure, the second template has been properly parsed, and you can't write a template which outputs invalid HTML or doesn't escape things properly. And hopefully it's easier to read.

To try this template, run

`./compile.sh && ./test.sh run examples/choose.html examples/choose_view.json`

and to see the AST, run

` ./compile.sh && ./test.sh tree examples/choose.html`

Make sure you have `javac` and `jruby` on your `PATH`.


## Language overview

A template is a whitespace separated list of:

* Values from the view, which are simple bare words. Use `.` as a separator to access nested values. `.` on it's own refers to the current view, eg a value when iterating over a list.

* HTML tags with attributes. These form part of the language, and so are properly parsed and validated. Tags must be balanced.

* Literal strings in `" "`, with quotes escaped as `\"`

* Functions, which take an optional anonymous block and zero or more named blocks. (see `if()` and `case()` in the example above). Blocks can be named with quoted strings using the literal syntax, or just bare words.

* Lists, which are just zero or more of the above in `[ ]`

* Enclosing view blocks, `^ { }`, the number of `^` characters indicating how many enclosing blocks to traverse, and then the block is rendered with that view.

* C++ style comments, which begin with `//` and end at the end of the line


## Views

The template is rendered from a view. This is a nested data structure, somewhat like JSON but the actual implementation dependent on the Driver. Values in the template refer to named properties in the view.

There is a concept of the "current view". This starts at the root of the data structure. Values are looked up starting from the current view.

`each()` and `with()` change the current view. `each()` changes the view to each element of a list in order, rendering the block for each. `with()` just changes it and renders the block.

Sometimes you need to refer to values which are outside the current view. Use an enclosing view block, for example, to access the 'x' value in the enclosing view, use `^{x}`.


### Attributes on Elements

If your HTML element is just has attributes with constant values (as literals) then your element is written exactly as it would be in normal HTML files, and will be output exactly as is.

Otherwise, a single value follows, such as `<a href=action>`, and the `action` value will be output, properly escaped.

That single value may be a list, eg `<div class=["name1" otherClass]>`, in which case, a space separated value will be generated. If `otherClass` is missing, just `<div class="name1">` will be output. If the attribute value is a list which evaluates as empty, the attribute will be omitted from the output entirely.

Functions can be used for attribute values.


## Implementation

The prototype has a simple hand-written recursive descent parser which outputs a thread-safe AST. This AST is then rendered with a Driver which provides language specific implentations to retrieve values from the view.

Currently there are two example drivers (neither terribly useful in practise), one for nested Java structures, and one for a JRuby data structure output by JRuby's JSON parser.

All the rendering functions keep track of the context, so escaping and other features is context aware.

Running the tests requires JRuby, and the build system isn't worthy of the name.


## Running the tests

Make sure you have `javac` and `jruby` on your `PATH`, then run

`./compile.sh && ./test.sh`

The tests can be found in the `test-case` directory, and `test.rb` is a simple JRuby script to run them.


## Functions

The multiple block syntax allows control flow to be implemented as simple functions. `if()` is just a function which takes a single argument, an anonymous block, and an optional `else` block.

Drivers will be able to implement their own functions, and provide facilities for users to add their own functions for their specific templates (carefully, to make it hard to XSS themselves).

Built in functions are:

### if(value) & unless(value)

A simple conditional control flow function. The Driver determines whether `value` is truthy. The anonymous block is rendered if it is, otherwise the `else` block.

### each(value)

The Driver returns an Iterable of nested views for `value`, then the anonymous block is rendered once per view in that list.

### with(value)

The view is moved to the `value`, then the anonymous block is rendered.

### case(value)

Evaluate `value` as a string, then render the named block with that name. If that block doesn't exist, render the anonymous block.

### unsafeHTML(value)

Evaluate `value` as a string, then output directly in the template without escaping. A parse error is thrown if it is used outside the text context (eg can't be used in an attribute value).

### include("template")

Include another template in the rendered output, controlled by the Driver.


## TODO

* URL generation and parameter escaping (should support building URLs from elements (eg paths and ids), and building parameters from dictionaries, static parameters etc)

* 'local variables' to remember values as view is traversed

* Destructuring arrays into local variables for compact views & clarity

* Security review, make sure everything escapes properly, and see if there are any more security features which can be implemented

* Drivers for JRuby (a proper one) and Rhino JavaScript

* Javadocs, build system, etc, so it can be used in other projects

* Linter to find more problems in templates (eg inline JavaScript, improperly escaped URLs)

* Internationalisation support


## Contributing

If you're interested in this project, then contributions would be very welcome.

* The most useful thing you can do is try writing some templates in this language and providing feedback. Perhaps you could convert your most complex templates and try running them?

* More test cases for any edge cases you can think of would be wonderful. I'd like to get to 100% test coverage, as this seems appropriate for a security focused project.

* And just as importantly, write bad templates and make sure they throw parse errors and those errors are useful.

* Suggestions on how to write it better, and improvements and fixes.

If you submit a pull request, I'll ask you to confirm you're happy to license your code under the MPLv2 before I merge it.

Thank you!
