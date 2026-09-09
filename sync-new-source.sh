#!/bin/bash
set -e

# Roda esse script DENTRO da pasta clyvocare-devops (seu repo, o que você pode editar)

NEW_REPO="https://github.com/VitoriaMaglio/ClyvoCareSC.git"
TMP_DIR="/tmp/clyvocare-new-src"

echo ">> 1) Clonando o repo novo (somente leitura, em pasta temporária)..."
rm -rf "$TMP_DIR"
git clone --depth 1 "$NEW_REPO" "$TMP_DIR"

echo ">> 2) Removendo o código Java antigo (Maven) do repo do devops..."
rm -rf src pom.xml mvnw mvnw.cmd .mvn

echo ">> 3) Copiando o código Gradle novo para o repo do devops..."
cp -r "$TMP_DIR/src" .
cp "$TMP_DIR/build.gradle" .
cp "$TMP_DIR/settings.gradle" .
cp -r "$TMP_DIR/gradle" .
cp "$TMP_DIR/gradlew" .
cp "$TMP_DIR/gradlew.bat" .
chmod +x gradlew

echo ">> 4) Gerando script_bd.sql a partir das migrations do Flyway (V1 a V14)..."
cat src/main/resources/db/migration/V*.sql > script_bd.sql
echo "   -> script_bd.sql gerado com $(grep -c '' script_bd.sql) linhas."

echo ">> 5) Pronto. Revise as mudanças antes de commitar:"
echo "   git status"
echo "   git add -A"
echo "   git commit -m 'chore: atualiza codigo Java para versao nova (Gradle + Flyway + JWT)'"
echo "   git push"
echo ""
echo "Lembrete: o Dockerfile e o docker-compose.yml já foram adaptados para Gradle/Java 25/Flyway"
echo "separadamente - confira se estão na raiz do repo antes de builder a imagem."
