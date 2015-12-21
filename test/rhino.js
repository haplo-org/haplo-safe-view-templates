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
