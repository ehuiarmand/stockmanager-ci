package com.inphb.icgl.stocks.utils;

import com.inphb.icgl.stocks.model.Produit;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportUtil {

    private ExportUtil() {
    }

    public static void exporterXLSX(List<Produit> produits, File fichier) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Produits");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            XSSFCellStyle styleEntete = workbook.createCellStyle();
            styleEntete.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 27, (byte) 58, (byte) 107}, null));
            styleEntete.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleEntete.setAlignment(HorizontalAlignment.CENTER);
            XSSFFont fontBlanc = workbook.createFont();
            fontBlanc.setColor(IndexedColors.WHITE.getIndex());
            fontBlanc.setBold(true);
            styleEntete.setFont(fontBlanc);

            XSSFCellStyle styleTitre = workbook.createCellStyle();
            styleTitre.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 15, (byte) 125, (byte) 25}, null));
            styleTitre.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleTitre.setAlignment(HorizontalAlignment.CENTER);
            XSSFFont fontTitre = workbook.createFont();
            fontTitre.setColor(IndexedColors.WHITE.getIndex());
            fontTitre.setBold(true);
            fontTitre.setFontHeightInPoints((short) 14);
            styleTitre.setFont(fontTitre);

            XSSFCellStyle styleAlerte = workbook.createCellStyle();
            styleAlerte.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 255, (byte) 200, (byte) 200}, null));
            styleAlerte.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row titre = sheet.createRow(0);
            Cell titreCell = titre.createCell(0);
            titreCell.setCellValue(fichier.getName() + " - StockManager CI");
            titreCell.setCellStyle(styleTitre);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));

            Row meta = sheet.createRow(2);
            meta.createCell(0).setCellValue("StockManager CI - Export Produits du " + LocalDateTime.now().format(formatter) + " | Quincaillerie KOFFI & FRERES");

            String[] entetes = {"Reference", "Designation", "Categorie", "Fournisseur", "Prix (FCFA)", "Quantite", "Stock Min", "Unite", "Alerte"};
            Row ligneEntete = sheet.createRow(4);
            for (int i = 0; i < entetes.length; i++) {
                Cell cell = ligneEntete.createCell(i);
                cell.setCellValue(entetes[i]);
                cell.setCellStyle(styleEntete);
                sheet.setColumnWidth(i, i == 1 || i == 3 ? 6500 : 4200);
            }

            int numLigne = 5;
            int alertes = 0;
            for (Produit p : produits) {
                Row row = sheet.createRow(numLigne++);
                boolean enAlerte = p.getQuantiteStock() <= p.getStockMinimum();
                if (enAlerte) {
                    alertes++;
                }
                String[] valeurs = {
                        p.getReference(),
                        p.getDesignation(),
                        p.getNomCategorie(),
                        p.getNomFournisseur(),
                        p.getPrixUnitaire() == null ? "0" : p.getPrixUnitaire().toPlainString(),
                        String.valueOf(p.getQuantiteStock()),
                        String.valueOf(p.getStockMinimum()),
                        p.getUnite(),
                        enAlerte ? "ALERTE" : "OK"
                };
                for (int i = 0; i < valeurs.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(valeurs[i] == null ? "" : valeurs[i]);
                    if (enAlerte) {
                        cell.setCellStyle(styleAlerte);
                    }
                }
            }

            Row resume = sheet.createRow(numLigne + 1);
            resume.createCell(0).setCellValue("Total : " + produits.size() + " produit(s)  |  dont " + alertes + " en alerte stock  |  Export : " + LocalDateTime.now().format(formatter));

            try (FileOutputStream fos = new FileOutputStream(fichier)) {
                workbook.write(fos);
            }
        }
    }

    public static void exporterPDF(List<Produit> produits, File fichier) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float margin = 36f;
                float y = pageHeight - margin;
                float rowHeight = 18f;
                float[] widths = {70f, 170f, 105f, 105f, 75f, 60f, 60f, 55f};
                float tableWidth = sum(widths);
                float startX = margin + 8f;

                setFillColor(content, 27, 58, 107);
                content.addRect(margin, y - 24f, pageWidth - margin * 2, 24f);
                content.fill();
                writeText(content, "StockManager CI - Export PDF Produits", startX, y - 17f, fontBold, 13, 255, 255, 255);

                y -= 38f;
                writeText(content,
                        "Export du " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                + " | Quincaillerie KOFFI & FRERES",
                        startX, y, fontRegular, 10, 70, 84, 104);

                y -= 28f;
                String[] headers = {"Reference", "Designation", "Categorie", "Fournisseur", "Prix", "Qte", "Min", "Statut"};
                drawRow(content, startX, y, widths, headers, true, fontRegular, fontBold);
                y -= rowHeight;

                int lignes = 0;
                for (Produit produit : produits) {
                    if (y < margin + 30f) {
                        break;
                    }

                    String statut = produit.getQuantiteStock() <= produit.getStockMinimum() ? "ALERTE" : "OK";
                    String[] values = {
                            safe(produit.getReference()),
                            trim(safe(produit.getDesignation()), 30),
                            trim(safe(produit.getNomCategorie()), 18),
                            trim(safe(produit.getNomFournisseur()), 18),
                            formatPrix(produit.getPrixUnitaire()),
                            String.valueOf(produit.getQuantiteStock()),
                            String.valueOf(produit.getStockMinimum()),
                            statut
                    };
                    drawRow(content, startX, y, widths, values, false, fontRegular, fontBold);
                    y -= rowHeight;
                    lignes++;
                }

                float footerY = margin - 6f;
                writeText(content,
                        "Total produits: " + produits.size() + "    |    Lignes exportees sur cette page: " + lignes,
                        startX, footerY, fontBold, 10, 27, 58, 107);
            }

            document.save(fichier);
        }
    }

    private static void drawRow(PDPageContentStream content, float startX, float y, float[] widths, String[] values,
                                boolean header, PDFont fontRegular, PDFont fontBold) throws IOException {
        float x = startX;
        for (int i = 0; i < widths.length; i++) {
            boolean centerCell = header;
            if (header) {
                setFillColor(content, 46, 94, 170);
            } else {
                setFillColor(content, i == values.length - 1 && "ALERTE".equals(values[i]) ? 255 : 250,
                        i == values.length - 1 && "ALERTE".equals(values[i]) ? 232 : 252,
                        i == values.length - 1 && "ALERTE".equals(values[i]) ? 232 : 255);
            }
            content.addRect(x, y - 14f, widths[i], 18f);
            content.fill();
            setStrokeColor(content, 210, 220, 235);
            content.addRect(x, y - 14f, widths[i], 18f);
            content.stroke();

            if (header) {
                writeCellText(content, values[i], x, widths[i], y - 2f, fontBold, 9, 255, 255, 255, centerCell);
            } else {
                int red = i == values.length - 1 && "ALERTE".equals(values[i]) ? 192 : 51;
                int green = i == values.length - 1 && "ALERTE".equals(values[i]) ? 0 : 51;
                int blue = i == values.length - 1 && "ALERTE".equals(values[i]) ? 32 : 51;
                writeCellText(content, trim(values[i], 28), x, widths[i], y - 2f, fontRegular, 8, red, green, blue, centerCell);
            }
            x += widths[i];
        }
    }

    private static void writeText(PDPageContentStream content, String text, float x, float y,
                                  PDFont font, int size,
                                  int red, int green, int blue) throws IOException {
        content.beginText();
        content.setFont(font, size);
        setFillColor(content, red, green, blue);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }

    private static void writeCellText(PDPageContentStream content, String text, float cellX, float cellWidth, float y,
                                      PDFont font, int size, int red, int green, int blue, boolean center) throws IOException {
        float x = cellX + 4f;
        if (center) {
            float textWidth = font.getStringWidth(text) / 1000f * size;
            x = cellX + Math.max(4f, (cellWidth - textWidth) / 2f);
        }
        writeText(content, text, x, y, font, size, red, green, blue);
    }

    private static void setFillColor(PDPageContentStream content, int red, int green, int blue) throws IOException {
        content.setNonStrokingColor(red / 255f, green / 255f, blue / 255f);
    }

    private static void setStrokeColor(PDPageContentStream content, int red, int green, int blue) throws IOException {
        content.setStrokingColor(red / 255f, green / 255f, blue / 255f);
    }

    private static String formatPrix(BigDecimal prix) {
        return prix == null ? "0" : prix.toPlainString();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String trim(String value, int max) {
        if (value == null || value.length() <= max) {
            return safe(value);
        }
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static float sum(float[] values) {
        float total = 0f;
        for (float value : values) {
            total += value;
        }
        return total;
    }
}
