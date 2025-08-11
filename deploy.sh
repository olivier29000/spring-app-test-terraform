#!/bin/bash

# ✅ Fonction utilitaire pour extraire une valeur XML d'une balise simple du pom.xml
get_pom_value() {
  local tag=$1
  grep -m 1 "<$tag>" pom.xml | sed -E "s/.*<$tag>(.*)<\/$tag>.*/\1/"
}

# 🔐 Charger les variables d’environnement depuis .env proprement
if [ -f .env ]; then
  set -a
  source .env
  set +a
else
  echo "❌ Fichier .env introuvable"
  exit 1
fi

# 📦 Extraire les infos du pom.xml
FINAL_NAME=$(get_pom_value "finalName")
ARTIFACT_ID=$(get_pom_value "artifactId")
VERSION=$(get_pom_value "version")

# 🧠 Nom final de l'application
if [ -n "$FINAL_NAME" ]; then
  JAR_NAME="${FINAL_NAME}.jar"
  APP_NAME="$FINAL_NAME"
else
  JAR_NAME="${ARTIFACT_ID}-${VERSION}.jar"
  APP_NAME="$ARTIFACT_ID"
fi

JAR_FILE="target/$JAR_NAME"

echo "📦 Compilation Maven..."
mvn clean package -DskipTests

# ✅ Vérifier que le JAR a été généré
if [ ! -f "$JAR_FILE" ]; then
  echo "❌ Le fichier $JAR_FILE n'a pas été généré."
  exit 1
fi

# 📤 Copier le JAR dans le dossier terraform/
echo "📁 Copie de $JAR_FILE vers terraform/"
cp "$JAR_FILE" terraform/

# 📂 Se rendre dans terraform/
cd terraform || exit

# 🧽 Nettoyer les retours à la ligne dans les secrets (au cas où)
AWS_ACCESS_KEY_CLEAN=$(echo "$AWS_ACCESS_KEY" | tr -d '\r\n')
AWS_SECRET_KEY_CLEAN=$(echo "$AWS_SECRET_KEY" | tr -d '\r\n')
DB_USERNAME_CLEAN=$(echo "$DB_USERNAME" | tr -d '\r\n')
DB_PASSWORD_CLEAN=$(echo "$DB_PASSWORD" | tr -d '\r\n')

# 📝 Générer le fichier terraform.auto.tfvars.json proprement
echo "📝 Génération de terraform.auto.tfvars.json..."
cat > terraform.auto.tfvars.json <<EOF
{
  "eb_app_name": "$APP_NAME",
  "bucket_name": "bucket-$APP_NAME",
  "eb_env_name": "env-$APP_NAME",
  "aws_access_key": "$AWS_ACCESS_KEY_CLEAN",
  "aws_secret_key": "$AWS_SECRET_KEY_CLEAN",
  "db_identifier": "mysqldb-$APP_NAME",
  "db_name": "db_app",
  "db_username": "$DB_USERNAME_CLEAN",
  "db_password": "$DB_PASSWORD_CLEAN"
}
EOF

# 🚀 Déploiement avec Terraform
terraform init
terraform apply -auto-approve
