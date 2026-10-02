This provides a service loader interface for handling the in-process formatting
of scenarios.

You will want to make sure that an implementation of the service loader is also
available.  Most things should use `azuki-script-formatter-ktlint`, which
targets a version of KtLint that is compatible with Azuki's target Kotlin
version.
