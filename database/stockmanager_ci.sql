CREATE DATABASE IF NOT EXISTS stockmanager_ci
CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE stockmanager_ci;

CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_complet VARCHAR(150) NOT NULL,
    login VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(64) NOT NULL,
    role ENUM('ADMIN','GESTIONNAIRE') DEFAULT 'GESTIONNAIRE',
    actif TINYINT(1) DEFAULT 1,
    two_factor_enabled TINYINT(1) NOT NULL DEFAULT 0,
    two_factor_secret VARCHAR(64) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS fournisseurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100),
    adresse VARCHAR(255),
    ville VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(30) NOT NULL UNIQUE,
    designation VARCHAR(200) NOT NULL,
    id_categorie INT,
    id_fournisseur INT,
    prix_unitaire DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    quantite_stock INT NOT NULL DEFAULT 0,
    stock_minimum INT NOT NULL DEFAULT 5,
    unite VARCHAR(30) DEFAULT 'piece',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categorie) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (id_fournisseur) REFERENCES fournisseurs(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS mouvements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_produit INT NOT NULL,
    type_mouvement ENUM('ENTREE','SORTIE') NOT NULL,
    quantite INT NOT NULL,
    motif VARCHAR(255),
    id_utilisateur INT,
    date_mouvement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_produit) REFERENCES produits(id) ON DELETE CASCADE,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id) ON DELETE SET NULL
);

INSERT INTO utilisateurs (nom_complet, login, mot_de_passe, role) VALUES
('KOFFI N''Guessan (Administrateur)', 'admin', SHA2('admin123',256), 'ADMIN'),
('TRAORE Aminata (Gestionnaire)', 'aminata', SHA2('aminata2026',256), 'GESTIONNAIRE'),
('GBEDE NEE ADOU BOMO', 'gbede', SHA2('gbede',256), 'ADMIN'),
('KUYO NEE NATEY NATHALIE', 'natey', SHA2('natey2026',256), 'ADMIN'),
('TANOH EHUI Armand', 'armand', SHA2('armand2026',256), 'ADMIN');

INSERT INTO categories (libelle, description) VALUES
('Materiaux', 'Materiaux de construction pour chantier'),
('Plomberie', 'Tuyaux, robinets et accessoires sanitaires'),
('Electricite', 'Cables, interrupteurs et materiel electrique'),
('Peinture', 'Peintures, diluants et accessoires'),
('Outillage', 'Outils manuels et equipements de quincaillerie');

INSERT INTO fournisseurs (nom, telephone, email, adresse, ville) VALUES
('SIMAT Adjame', '27 20 31 12 00', 'adjame@simat.ci', 'Boulevard Nangui Abrogoua, Adjame', 'Abidjan'),
('LAFARGE CI Distribution', '27 21 75 10 00', 'contact@lafarge.ci', 'Zone Portuaire, Treichville', 'Abidjan'),
('SOTACI Materiaux', '27 20 30 48 00', 'commercial@sotaci.ci', 'Rue des Carrossiers, Zone 4', 'Abidjan'),
('IVOIRE ELECTRO PRO', '27 22 40 15 20', 'contact@ivoirelectro.ci', 'Avenue Chardy, Plateau', 'Abidjan'),
('SAN-PEDRO PEINTURE', '27 34 71 22 10', 'vente@sppeinture.ci', 'Quartier Bardot', 'San-Pedro');

INSERT INTO produits (reference, designation, id_categorie, id_fournisseur, prix_unitaire, quantite_stock, stock_minimum, unite) VALUES
('MAT-CIM-425', 'Ciment CPI 42.5', 1, 2, 5200.00, 120, 30, 'sac'),
('MAT-TOLE-3M', 'Tole ondulee 3m', 1, 3, 18500.00, 40, 12, 'piece'),
('PLB-ROB-15', 'Robinet laiton 15mm', 2, 1, 6500.00, 22, 10, 'piece'),
('ELE-CAB-25', 'Cable electrique 2.5mm', 3, 4, 950.00, 300, 80, 'metre'),
('PNT-BLNC-20', 'Peinture blanche 20L', 4, 5, 28500.00, 9, 12, 'seau');
