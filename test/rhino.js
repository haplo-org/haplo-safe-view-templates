/* Haplo Safe View Templates                          http://haplo.org
 * (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.         */

var assert = function(t, description) {
    $testcount++;
    if(t) {
        $testpass++
    } else {
        java.lang.System.out.println("\nFAIL in test/rhino.js: "+description);
    }
}
var assertEqual = function(a, b, description) {
    if(a !== b) { description = (description||'') + ": " + JSON.stringify(a)+" !== "+JSON.stringify(b); }
    assert(a === b, description);
}
var assertException = function(fn, expectedMessage) {
    var message = "(No exception thrown)";
    try { fn() }
    catch(e) { message = e.message; }
    assertEqual(message, expectedMessage);
}

// --------------------------------------------------------------------------

var template1 = new $HaploTemplate("<div> x </div>");
assertEqual(template1.render({x:4}), "<div>4</div>", "Simple render");
assertEqual(template1.render({x:"Hello"}), "<div>Hello</div>", "Simple render, same template");
assertEqual(template1({x:17}), "<div>17</div>", "Simple render as function");
assertEqual(template1({x:"Pong"}), "<div>Pong</div>", "Simple render as function, 2");

// --------------------------------------------------------------------------
// Can use JS objects with getters as views
var View = function() { this.$nextX = 0; }
View.prototype.__defineGetter__('x', function() { return this.$nextX++; });
View.prototype.__defineGetter__('y', function() { return ["Hello "+this.x, "World"]; });
var templateView = new View();
assertEqual(template1.render(templateView), "<div>0</div>", "Values from getters (0)");
assertEqual(template1.render(templateView), "<div>1</div>", "Values from getters (1)");
assertEqual(template1.render(templateView), "<div>2</div>", "Values from getters (2)");
assertEqual(template1.render(new View()), "<div>0</div>", "Values from getters (new)");
var template2 = new $HaploTemplate('<span> each(y) { . "/" } </span>');
assertEqual(template2.render(templateView), "<span>Hello 3/World/</span>", "More exciting values from getters");

// --------------------------------------------------------------------------
// Deferred rendering

var deferred1 = template1.deferredRender({x:"Deferred X"});
assert(deferred1 instanceof $HaploTemplateDeferredRender, "deferred is right type");
var template2 = new $HaploTemplate("<span> render(def) </span>");
assertEqual(template2.render({def:deferred1}), "<span><div>Deferred X</div></span>", "Deferred render");
assertEqual(template2.render({def:deferred1}), "<span><div>Deferred X</div></span>", "Deferred render (2)");

// --------------------------------------------------------------------------
// Template functions defined in JS

var JSFunctions = {
    notafunction: "Hello!",
    jsfn1: function() { return 'FUNCTIONONE'; },
    jsargs: function() {
        return "`"+this.functionName+":"+Array.prototype.join.call(arguments, ",")+"`";
    },
    writethis: function(str) {
        this.write(str);
    },
    writehtml: function(x) {
        this.unsafeWriteHTML("<- ").
            unsafeWriteHTML(""+x).
            unsafeWriteHTML(" ->");
    },
    writeblocks: function(y) {
        this.write("Hello ");
        if(!this.hasBlock()) { throw new Error("no anon block"); }
        this.writeBlock().
            write(" world - ").
            write(y);
        if(this.hasBlock("extras")) {
            this.write(" EXTRAS ").writeBlock("extras");
        }
    },
    badBlockName: function() {
        this.writeBlock(346);
    },
    badBlockName2: function() {
        this.hasBlock(346);
    },
    rendering: function(thing) {
        this.render(thing);
    },
    checkContext: function(context) {
        this.assertContext(context).write("Context: "+context);
    }
};
$haploTemplateFunctionFinder = function(name) {
    return JSFunctions[name];
};


assertException(function() {
    (new $HaploTemplate('<span> notafunction() </span>', "testbad")).render({});
}, "org.haplo.template.html.RenderException: When rendering template 'testbad': JavaScript template function notafunction() must be implemented by a function");

var templateWithFns1 = new $HaploTemplate("<div> jsfn1() </div>");
assertEqual(templateWithFns1.render({}), "<div>FUNCTIONONE</div>", "Simple JS template function");
var templateWithFns1b = new $HaploTemplate("<div data-value=jsfn1()> </div>");
assertEqual(templateWithFns1b.render({}), '<div data-value="FUNCTIONONE"></div>', "Simple JS template function as attribute");
var templateWithFns2 = new $HaploTemplate('<div> jsargs(a "b") </div>');
assertEqual(templateWithFns2.render({a:874}), "<div>`jsargs:874,b`</div>", "JS template function with args");
assertEqual(templateWithFns2.render({a:"hello there"}), "<div>`jsargs:hello there,b`</div>", "JS template function with args (2)");

var templateWithFns3 = new $HaploTemplate('<div> jsfn1() " " jsfn1() " " jsargs() " " jsfn1() </div>');
assertEqual(templateWithFns3.render({}), "<div>FUNCTIONONE FUNCTIONONE `jsargs:` FUNCTIONONE</div>", "repeated use of a single function to check function caching");

var templateWithFns4 = new $HaploTemplate('<div> writethis(value) </div>');
assertEqual(templateWithFns4.render({value:"<hello>&"}), "<div>&lt;hello&gt;&amp;</div>", "function which uses write()");

var templateWithFns5 = new $HaploTemplate('<div> writehtml(x) </div>');
assertEqual(templateWithFns5.render({x:56}), "<div><- 56 -></div>", "function which uses unsafeWriteHTML()");

var templateWithFns6 = new $HaploTemplate('<span> writeblocks(p) { "(" value ")"} </span>');
assertEqual(templateWithFns6.render({p:23, value:"lovely"}), "<span>Hello (lovely) world - 23</span>", "function which uses writeBlock()");

var templateWithFns7 = new $HaploTemplate('<span> writeblocks(p) { "ping " value } extras { extra "!" } </span>');
assertEqual(templateWithFns7.render({p:24, value:"x", extra:"xyz"}), "<span>Hello ping x world - 24 EXTRAS xyz!</span>", "function which uses writeBlock() with named blocks");

var templateWithFns8 = new $HaploTemplate('<span> rendering(thing) </span>');
assertEqual(templateWithFns8.render({thing:templateWithFns1}), "<span><div>FUNCTIONONE</div></span>");
var templateWithFns8 = new $HaploTemplate('<span> rendering(thing) </span>');
assertEqual(templateWithFns8.render({thing:templateWithFns1.deferredRender()}), "<span><div>FUNCTIONONE</div></span>");

assertException(function() {
    templateWithFns8.render({thing:"string"});
}, "org.haplo.template.html.RenderException: When rendering template 'undefined': In rendering(), bad object passed to render()");
assertException(function() {
    (new $HaploTemplate('<span data-value=rendering(thing)> "hello" </span>', "testbad")).render({thing:"a"});
}, "org.haplo.template.html.RenderException: When rendering template 'testbad': In rendering(), can only use render() in text context");
assertException(function() {
    (new $HaploTemplate('<span> badBlockName() </span>', "testbad")).render({thing:"a"});
}, "org.haplo.template.html.RenderException: When rendering template 'testbad': In badBlockName(), bad block name");
assertException(function() {
    (new $HaploTemplate('<span> badBlockName2() </span>', "testbad")).render({thing:"a"});
}, "org.haplo.template.html.RenderException: When rendering template 'testbad': In badBlockName2(), bad block name");

var templateCheckContext = new $HaploTemplate('<span> checkContext(context) </span>');
assertEqual(templateCheckContext.render({context:"TEXT"}), "<span>Context: TEXT</span>", "Assert context");
assertException(function() {
    templateCheckContext.render({context:"carrots"})
}, "org.haplo.template.html.RenderException: When rendering template 'undefined': In checkContext(), Unknown context name: 'carrots'");
assertException(function() {
    templateCheckContext.render({context:"ATTRIBUTE_VALUE"});
}, "org.haplo.template.html.RenderException: When rendering template 'undefined': In checkContext(), must be used in ATTRIBUTE_VALUE context, attempt to use in TEXT context");
var templateCheckContext2 = new $HaploTemplate('<span data-value=checkContext(context)></span>');
assertEqual(templateCheckContext2.render({context:"ATTRIBUTE_VALUE"}), '<span data-value="Context: ATTRIBUTE_VALUE"></span>', "Assert context");
assertException(function() {
    templateCheckContext2.render({context:"TEXT"});
}, "org.haplo.template.html.RenderException: When rendering template 'undefined': In checkContext(), must be used in TEXT context, attempt to use in ATTRIBUTE_VALUE context");

// Make sure parser configuration is active
assertException(function() {
    new $HaploTemplate("<div>badFunction()</div>");
}, "org.haplo.template.html.ParseException: Error at line 1 character 18: badFunction doesn't validate");

// Call platform defined function
var templateWithPlatformFns = new $HaploTemplate("<div> generic-function() </div>");
assertEqual(templateWithPlatformFns.render({}), "<div>TEST GENERIC FUNCTION RENDER</div>", "JS template calls platform function");

// Include platform templates
var templateWithIncludedTemplate = new $HaploTemplate("<div> template:template1() </div>");
assertEqual(templateWithIncludedTemplate.render({value1:"Hello!"}), "<div><b>Included Template 1: Hello!</b></div>", "JS template includes other template");

// --------------------------------------------------------------------------
// Iterating over array-like things

var IterableThing = function() {};
IterableThing.prototype.__defineGetter__('length', function() { return 3; });
IterableThing.prototype[2] = 'In prototype';
var arrayLikeThingInstance = new IterableThing();
arrayLikeThingInstance[0] = 'Hello';
arrayLikeThingInstance[1] = 'World';

var iterableTemplate = new $HaploTemplate('<div> each(a) { "^" . "$" } </div>');
assertEqual(iterableTemplate.render({a:arrayLikeThingInstance}), "<div>^Hello$^World$^In prototype$</div>");
