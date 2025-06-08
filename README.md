# Python Code Quality Tools for Intellij Platforms

### This is a multirepo for [Pylint](https://plugins.jetbrains.com/plugin/26358-pylint) and [Mypy](https://plugins.jetbrains.com/plugin/25888-mypy) plugins, with a twist.
Original origins: [pylint plugin](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#configuration-intellij-extension) and [mypy plugin](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#configuration-intellij-extension).\
Find plugin's README within the subproject on its release branch. (le twist, explained later)

### Yeah, but why?
To make maintenance easier, I wanted the two plugins to share most of their sources.
Trying to avoid multirepo, I tried gradle's [sourceControl](https://blog.gradle.org/introducing-source-dependencies),
but the lack of features and uncomfortable development flow made me leave it.
After that I gave [jitpack](https://www.jitpack.io/) a shot, but the development flow was still cumbersome.\
So I ended up with including everything in a single repository, this way the common part doesn't have its own
releasing cycle. It made development (and especially refactoring) way easier.\
When common part becomes stable, I'll likely switch back to multirepo.

### The twist
To enable releasing multiple plugins - that share some of their sources - from a single repository, but still keeping their
releases apart, they live on their own release branches. I rebase them on the common part's branch whenever I want
to keep up with its changes.

#### With one of a kind solution come one of a kind issues
Yup, expected, still seems to be the optimal way for now.
