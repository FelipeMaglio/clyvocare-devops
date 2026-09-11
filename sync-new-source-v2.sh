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

echo ">> 5) Habilitando o Swagger (o ClyvoCareSC original não vem com ele)..."
echo "   -> Adicionando dependencia springdoc no build.gradle..."
if ! grep -q "springdoc-openapi" build.gradle; then
  sed -i "s#implementation 'org.springframework.boot:spring-boot-starter-webmvc'#implementation 'org.springframework.boot:spring-boot-starter-webmvc'\n\timplementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1'#" build.gradle
  echo "   -> springdoc adicionado."
else
  echo "   -> springdoc ja estava presente, nada a fazer."
fi

SECURITY_CONFIG="src/main/java/com/fiap/clyvocaresc/security/SecurityConfig.java"
echo "   -> Liberando as rotas do Swagger no SecurityConfig..."
if [ -f "$SECURITY_CONFIG" ] && ! grep -q "swagger-ui" "$SECURITY_CONFIG"; then
  sed -i 's#\.requestMatchers("/api/auth/\*\*").permitAll()#.requestMatchers("/api/auth/**").permitAll()\n                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()#' "$SECURITY_CONFIG"
  echo "   -> Rotas do Swagger liberadas."
else
  echo "   -> Rotas do Swagger ja liberadas (ou arquivo nao encontrado - confira manualmente)."
fi

echo ">> 6) Pronto. Revise as mudanças antes de commitar:"
echo "   git status"
echo "   git add -A"
echo "   git commit -m 'chore: atualiza codigo Java para versao nova (Gradle + Flyway + JWT) + habilita Swagger'"
echo "   git push"
echo ""
echo "Lembrete: o Dockerfile e o docker-compose.yml já foram adaptados para Gradle/Java 25/Flyway"
echo "separadamente - confira se estão na raiz do repo antes de buildar a imagem."
