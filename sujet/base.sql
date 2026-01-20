CREATE TABLE configurations (
    cle VARCHAR(100) PRIMARY KEY,
    valeur TEXT NOT NULL,
    description TEXT,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Paramètres par défaut
INSERT INTO configurations (cle, valeur, description) VALUES
('max_tentatives_connexion', '3', 'Nombre maximum de tentatives de connexion avant blocage'),
('duree_blocage_minutes', '15', 'Durée de blocage automatique en minutes'),
('duree_session_heures', '24', 'Durée de validité d''une session en heures'),
('duree_refresh_token_jours', '7', 'Durée de validité du refresh token en jours');

CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    utilisateur_id UUID REFERENCES utilisateurs(id) ON DELETE CASCADE,
    token_acces TEXT NOT NULL,
    refresh_token TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration TIMESTAMP NOT NULL,
    date_derniere_activite TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_connexion VARCHAR(45),
    user_agent TEXT,
    est_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_sessions_utilisateur ON sessions(utilisateur_id);
CREATE INDEX idx_sessions_token ON sessions(token_acces);

-- Ajouter les colonnes manquantes
ALTER TABLE utilisateurs 
ADD COLUMN date_dernier_echec_connexion TIMESTAMP,
ADD COLUMN date_deblocage_automatique TIMESTAMP;

CREATE TABLE historique_connexions (
    id SERIAL PRIMARY KEY,
    utilisateur_id UUID REFERENCES utilisateurs(id) ON DELETE CASCADE,
    date_tentative TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    succes BOOLEAN NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    raison_echec VARCHAR(255) -- 'mot_de_passe_incorrect', 'compte_bloque', etc.
);

CREATE INDEX idx_historique_utilisateur ON historique_connexions(utilisateur_id);

change princi
