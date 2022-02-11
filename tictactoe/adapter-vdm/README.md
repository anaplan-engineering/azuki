# tictactoe:adapter-vdm

## Specification

Typically, we use the vdm-gradle-plugin to publish a specification artifact and then comsume that artifact in the adapter.
We have not yet been able to get composite builds working with VDM as it is proved impossible to manipulate the composite build mechanism.
For ease of demonstration we have packaged the specification into this project (see src/main/vdm), but use the same mechanisms (i.e. azuki-vdm and vdm-stub-generator) to work with those file in the same we would if we had extracted them from a published artifact.
