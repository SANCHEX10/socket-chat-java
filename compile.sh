diff --git a/compile.sh b/compile.sh
new file mode 100755
index 0000000000000000000000000000000000000000..f081abfd573d4dca9ce8ec56fa585a9592dadd34
--- /dev/null
+++ b/compile.sh
@@ -0,0 +1,6 @@
+#!/bin/bash
+set -e
+
+echo "Compilando Socket Chat Java..."
+javac src/*.java src/cli/*.java src/gui/*.java
+echo "Compilación completada correctamente."
