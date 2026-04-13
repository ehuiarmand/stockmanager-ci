package com.inphb.icgl.stocks.utils;

import com.inphb.icgl.stocks.model.Produit;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void exporterXlsxCreeUnFichierLisible() throws IOException {
        Path fichier = tempDir.resolve("produits.xlsx");

        ExportUtil.exporterXLSX(creerProduits(), fichier.toFile());

        assertTrue(Files.exists(fichier));
        try (XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(fichier))) {
            assertEquals("Produits", workbook.getSheetAt(0).getSheetName());
            assertEquals("Reference", workbook.getSheetAt(0).getRow(4).getCell(0).getStringCellValue());
            assertEquals("P001", workbook.getSheetAt(0).getRow(5).getCell(0).getStringCellValue());
        }
    }

    @Test
    void exporterPdfCreeUnDocumentAvecLesDonneesPrincipales() throws IOException {
        Path fichier = tempDir.resolve("produits.pdf");

        ExportUtil.exporterPDF(creerProduits(), fichier.toFile());

        assertTrue(Files.exists(fichier));
        try (PDDocument document = Loader.loadPDF(fichier.toFile())) {
            String texte = new PDFTextStripper().getText(document);
            assertTrue(texte.contains("StockManager CI - Export PDF Produits"));
            assertTrue(texte.contains("P001"));
            assertTrue(texte.contains("Marteau"));
        }
    }

    private List<Produit> creerProduits() {
        Produit produit = new Produit();
        produit.setReference("P001");
        produit.setDesignation("Marteau");
        produit.setNomCategorie("Outillage");
        produit.setNomFournisseur("Fournisseur A");
        produit.setPrixUnitaire(new BigDecimal("3500"));
        produit.setQuantiteStock(5);
        produit.setStockMinimum(10);
        produit.setUnite("piece");
        return List.of(produit);
    }
}
