# Bridge pattern defense guide

## Five minute walkthrough

**0:00–0:45 — Explain the problem.** Shapes and rendering technologies vary independently.
Without the bridge, backend-specific subclasses such as VectorCircle and RasterCircle multiply
combinations. Here Circle and Square describe geometry, while renderers choose the output format.

**0:45–1:30 — Point to the UML.** Shape is the Abstraction; Circle and Square are Refined
Abstractions; Renderer is the Implementor; VectorRenderer and RasterRenderer are Concrete
Implementors. BridgeDemo is the Client. Point to `private Renderer renderer` in Shape.
The link is composition through a field. No shape extends a renderer.

**1:30–2:30 — Show the code.** Circle.draw delegates its stored geometry to drawCircle.
Open Shape.setRenderer and explain the null check. Open each backend and compare SVG element
serialization with BufferedImage painting and PNG encoding. RenderTarget centralizes shared
output preparation. Client code selects the implementations, then works through Shape.

**2:30–3:30 — Live demonstration.** Run `./run.sh` or run BridgeDemo in IntelliJ. Show the four
output files. Open BridgeDemo and point out that the list and shape objects are constructed once.
`shape.setRenderer(raster)` changes the field on those objects. Place a debugger breakpoint
before that loop and after it if the instructor wants to inspect identity and renderer state.
Step through `shape.draw()` to see delegation reach RasterRenderer. Continue to the final switch
back to VectorRenderer. SVG files are overwritten on the last pass; PNG files remain available.

**3:30–4:15 — Explain Clean Code.** Use the six report excerpts. Focus on responsibility boundaries,
meaningful names, small methods, shared output handling, interface dependency, and invariant checks.

**4:15–5:00 — Explain trade-offs and validation.** Run `./test.sh`. Discuss the small amount of
extra structure, the fixed canvas, sequential output, and the shape-specific primitive interface.
A third renderer needs no abstraction changes. A new primitive would require interface changes.

## Likely questions and answers

**Why is this Bridge?** There are two separate hierarchies connected through a replaceable
Implementor reference. The goal is to vary shape abstractions and rendering implementations
independently. Runtime switching is visible evidence, but the design intent also matters.

**Where exactly is composition?** In Shape's Renderer field, injected by its constructor and
replaced by setRenderer. Circle inherits from Shape and delegates through renderer(). The hollow
UML diamond indicates sharing, since Circle and Square can reference the same renderer.

**Why not use inheritance for the backend?** A shape is not a renderer. Inheriting backend-specific
shape classes creates a separate class for each shape/backend combination. For m shapes and n
backends, the combined subclasses can require m × n classes; the separate concrete hierarchies
use m + n, plus their common contracts.

**How does Bridge differ from Adapter?** Bridge designs a boundary between two dimensions so
they can evolve separately. Adapter translates an existing incompatible interface into the
interface a client expects. This implementation starts with a common Renderer contract and has
no legacy API that needs translation.

**How does it differ from Strategy?** Both can use replaceable objects. Strategy emphasizes
interchanging an algorithm for one task. Here the purpose is separating the shape abstraction
hierarchy from the rendering implementation hierarchy. Similar object structures can express
different design intentions.

**Can I add another renderer?** Implement Renderer and inject the new instance. Shape, Circle
and Square do not change. RecordingRenderer in the tests demonstrates this directly.

**Can I add Triangle without changing the renderers?** Not with this narrow interface, because
it has no triangle or general path primitive. A refined shape that uses existing primitives can
be added unchanged; a new primitive needs a contract change across backends. A general drawPath
contract would increase extension flexibility but complicate this assignment.

**Does setRenderer rebuild a shape?** No. It replaces one reference. The final geometry fields
are unchanged. The switching test confirms that the replacement receives the same coordinates.

**What happens if the output directory is unwritable?** Directory creation or encoding throws
IOException. Shape.draw exposes this failure and the client does not falsely announce success.
The tests use a file where a directory is expected to exercise failure propagation.

**Why validate dimensions in shapes?** Positive size is a domain invariant. It is checked before
drawing and shared through requirePositive. Null renderer assignment is rejected while retaining
the previous renderer. Coordinates may be outside the canvas and are clipped consistently.

**What do the tests prove?** Exact delegation, switching and switching back, guards, actual SVG
structure, real PNG pixel geometry, observable I/O failure, and successful client output. Tests
do not claim visual equivalence at antialiased boundaries or concurrent-write safety.

**What would you improve in a production version?** Configurable canvas and styles, unique output
names, a general geometry contract, and a deliberate concurrency policy. Add these only when
requirements justify their complexity.

## Before attending

- Read every source file and explain it without reading a script.
- Run the demo and tests on the computer you will use in class with JDK 17.
- Make sure the instructor can open the public repository and that Moodle received the report.
- Confirm the official Week 4 submission and Week 5 defense dates in Moodle.
