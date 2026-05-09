#!/bin/bash

echo "╔════════════════════════════════════╗"
echo "║ Socket Chat Java - Menú Principal  ║"
echo "╚════════════════════════════════════╝"
echo ""
echo "Elige una opción:"
echo ""
echo "INTERFAZ GRÁFICA (GUI):"
echo "  1) Servidor GUI"
echo "  2) Cliente GUI"
echo ""
echo "LÍNEA DE COMANDOS (CLI):"
echo "  3) Servidor CLI"
echo "  4) Cliente CLI"
echo ""
echo "COMPILACIÓN:"
echo "  5) Compilar todo"
echo ""
read -p "Opción (1-5): " option

case $option in
  1)
    echo "Iniciando Servidor GUI..."
    java -cp src gui.ServerGUI
    ;;
  2)
    echo "Iniciando Cliente GUI..."
    java -cp src gui.ClientGUI
    ;;
  3)
    echo "Iniciando Servidor CLI..."
    java -cp src cli.ServerCLI
    ;;
  4)
    echo "Iniciando Cliente CLI..."
    java -cp src cli.ClientCLI
    ;;
  5)
    echo "Compilando..."
    ./compile.sh
    ;;
  *)
    echo "Opción inválida"
    ;;
esac
