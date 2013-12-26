om-business-model-canvas
========================

A collaborative business model canvas

This is interesting primarily as an early demonstration of a web app using Clojurescript,
David Nolan's Om bindings to React JS, and Firebase. These three pieces fit together rather
well; by centralizing app state in a single atom, it's very easy to store and synchronize 
with Firebase. 

This currently only barely works. 

The general setup is:

- The app state atom contains the document and a core.async channel for event handling. 
  - The event channel is accessible from any Om cursor, since they all have :om/state metadata
  - A *post-event* helper is used to make this transparent
- Event handlers are pure functions of app state, yielding more app state. 


Open questions:
- It's unclear how best to deal with transient UI state. This is currently done with the
get-state/set-state! mechanism, but that abstraction is already breaking down. I'd like to
be able to manipulate it from an event handler. 
