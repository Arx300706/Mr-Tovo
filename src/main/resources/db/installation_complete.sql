-- Installation complète de la base Mr Tovo (PostgreSQL).
-- À lancer avec psql depuis n'importe quel répertoire :
-- psql -h localhost -U postgres -d rh -v ON_ERROR_STOP=1 -f src/main/resources/db/installation_complete.sql
--
-- ATTENTION : schema.sql supprime et recrée le schéma public. Cette installation
-- efface donc toutes les tables et données déjà présentes dans la base ciblée.
-- Les migrations sont rejouées ensuite pour couvrir aussi les évolutions récentes.

\ir schema.sql
\ir data.sql
\ir migration_adresse.sql
\ir migration_cv_en_ligne.sql
