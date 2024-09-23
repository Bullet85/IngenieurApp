package com.example.mainapp;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    // Felder für die Eingaben im Nivellierungsformular
    private List<TextField> punktFields = new ArrayList<>();
    private List<TextField> rueckblickFields = new ArrayList<>();
    private List<TextField> vorblickFields = new ArrayList<>();
    private List<TextField> zwischenblickFields = new ArrayList<>();
    private TextField sumRueckblickField, sumVorblickField, sumSteigenField, sumFallenField, sumDeltaHField;

    // Zusätzliche Felder für Projektnummer, Datum und Bezugspunkt
    private TextField projektNummerField;
    private TextField datumField;
    private CheckBox bezugspunktCheckBox;

    // Zusätzliche Listen für Steigen- und Fallen-Labels
    private List<Label> steigenLabels = new ArrayList<>();
    private List<Label> fallenLabels = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ingenieur-App");

        // Hauptbuttons erstellen
        Button btnNivelliment = new Button("Nivelliment");
        Button btnStaticTest = new Button("Statischer Lastplattendruckversuch");
        Button btnDynamicTest = new Button("Dynamischer Lastplattendruckversuch");
        Button btnSeepTest = new Button("Versickerungsversuche");

        // Layout für die Buttons
        VBox vbox = new VBox(20, btnNivelliment, btnStaticTest, btnDynamicTest, btnSeepTest);
        Scene scene = new Scene(vbox, 300, 200);

        primaryStage.setScene(scene);
        primaryStage.show();

        // Event-Handling für den Nivelliment-Button
        btnNivelliment.setOnAction(e -> openNivellimentWindow(primaryStage));
    }

    // Öffne das Fenster für das Nivellierungsformular
    public void openNivellimentWindow(Stage primaryStage) {
        primaryStage.setTitle("Nivellierung Formular");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Projektnummer und Datum hinzufügen
        grid.add(new Label("Projektnummer"), 0, 0);
        projektNummerField = new TextField();
        grid.add(projektNummerField, 1, 0);

        grid.add(new Label("Datum"), 0, 1);
        datumField = new TextField();
        grid.add(datumField, 1, 1);

        grid.add(new Label("Punkt"), 0, 2);
        grid.add(new Label("Rückblick (r)"), 1, 2);
        grid.add(new Label("Vorblick (v)"), 2, 2);
        grid.add(new Label("Zwischenblick (z)"), 3, 2);
        grid.add(new Label("Steigen (+)"), 4, 2);
        grid.add(new Label("Fallen (-)"), 5, 2);

        // CheckBox für den Bezugspunkt (BP)
        bezugspunktCheckBox = new CheckBox("Bezugspunkt (BP)");
        grid.add(bezugspunktCheckBox, 2, 3); // Kontrollkästchen für den ersten Vorblick

        for (int i = 1; i <= 5; i++) {
            // Punktname
            TextField punktField = new TextField();
            punktFields.add(punktField);
            grid.add(punktField, 0, i + 3);

            TextField rueckblickField = new TextField();
            TextField vorblickField = new TextField();
            TextField zwischenblickField = new TextField();

            rueckblickFields.add(rueckblickField);
            vorblickFields.add(vorblickField);
            zwischenblickFields.add(zwischenblickField);

            grid.add(rueckblickField, 1, i + 3);
            grid.add(vorblickField, 2, i + 3);
            grid.add(zwischenblickField, 3, i + 3);

            // Neue Label-Instanzen für Steigen und Fallen
            Label steigenLabel = new Label();
            Label fallenLabel = new Label();

            steigenLabels.add(steigenLabel);
            fallenLabels.add(fallenLabel);

            grid.add(steigenLabel, 4, i + 3);  // Steigen
            grid.add(fallenLabel, 5, i + 3);   // Fallen
        }

        // Summenfelder
        sumRueckblickField = new TextField();
        sumVorblickField = new TextField();
        sumSteigenField = new TextField();
        sumFallenField = new TextField();
        sumDeltaHField = new TextField();

        grid.add(new Label("Σ Rückblick (Σr)"), 0, 9);
        grid.add(sumRueckblickField, 1, 9);
        grid.add(new Label("Σ Vorblick (Σv)"), 2, 9);
        grid.add(sumVorblickField, 3, 9);
        grid.add(new Label("Σ Steigen"), 4, 9);
        grid.add(sumSteigenField, 5, 9);
        grid.add(new Label("Σ Fallen"), 6, 9);
        grid.add(sumFallenField, 7, 9);
        grid.add(new Label("Höhenunterschied (Δh)"), 8, 9);
        grid.add(sumDeltaHField, 9, 9);

        // Berechnungen durchführen Button
        Button calculateButton = new Button("Berechnen");
        calculateButton.setOnAction(e -> calculateValues());

        // PDF speichern Button
        Button savePDFButton = new Button("Speichern als PDF");
        savePDFButton.setOnAction(e -> saveAsPDF());

        grid.add(calculateButton, 0, 10);
        grid.add(savePDFButton, 1, 10);

        // Neue Szene für das Formular
        Scene scene = new Scene(grid, 900, 500);
        primaryStage.setScene(scene);
    }

    private double getFieldValueOrZero(TextField textField) {
        String text = textField.getText();
        if (text == null || text.isEmpty()) {
            return 0.0; // Rückgabewert bei leerem Feld
        }
        // Ersetze Komma durch Punkt, um es als Dezimaltrennzeichen zu akzeptieren
        text = text.replace(",", ".");
        return Double.parseDouble(text); // Wert umwandeln, wenn das Feld gefüllt ist
    }


    // Berechnungen der Werte
    private void calculateValues() {
        double sumSteigen = 0, sumFallen = 0;
        double previousValue = 0; // Speichert den vorherigen Wert (Rückblick oder Vorblick)

        for (int i = 0; i < rueckblickFields.size(); i++) {
            double rueckblick = 0; // Initialisiere Rückblick nur ab dem zweiten Punkt
            double vorblick = getFieldValueOrZero(vorblickFields.get(i)); // Vorblick wird immer eingegeben

            // Wenn es der erste Punkt ist (i == 0), gibt es nur den Vorblick, keine Differenz
            if (i == 0) {
                // Zeige nur den Vorblick an
                steigenLabels.get(i).setText(String.format("Vor: %.3f", vorblick));
                fallenLabels.get(i).setText(""); // Kein Rückblick, daher keine Berechnung
                previousValue = vorblick; // Speichere den Vorblick als vorherigen Wert
            } else {
                // Ab dem zweiten Punkt gibt es Rückblicke
                rueckblick = getFieldValueOrZero(rueckblickFields.get(i));

                // Berechnung der Differenz zwischen dem vorherigen Wert (z.B. Vorblick) und dem Rückblick
                double delta = previousValue - rueckblick;

                // Speichere den aktuellen Rückblick als vorherigen Wert für die nächste Berechnung
                previousValue = rueckblick;

                // Anzeige der Differenz in Steigen/Fallen je nach Ergebnis
                if (delta < 0) {
                    // Negatives Ergebnis (ohne Vorzeichen bei Steigen)
                    sumSteigen += Math.abs(delta); // Addiere den Wert zu Steigen
                    steigenLabels.get(i).setText(String.format("%.3f", Math.abs(delta)));
                    fallenLabels.get(i).setText("0.000");
                } else if (delta > 0) {
                    // Positives Ergebnis
                    sumFallen += delta; // Addiere den Wert zu Fallen
                    fallenLabels.get(i).setText(String.format("%.3f", delta));
                    steigenLabels.get(i).setText("0.000");
                }
            }
        }

        // Summen anzeigen
        sumSteigenField.setText(String.format("%.3f", sumSteigen));
        sumFallenField.setText(String.format("%.3f", sumFallen));
    }








    // PDF speichern
    private void saveAsPDF() {
        // Speicherort wählen
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Speichern als PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                // PDF-Erstellung mit iText
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                document.add(new Paragraph("Nivellierungsformular"));
                document.add(new Paragraph("Projektnummer: " + projektNummerField.getText()));
                document.add(new Paragraph("Datum: " + datumField.getText()));

                float[] columnWidths = {150F, 150F, 150F, 150F, 150F};
                Table table = new Table(columnWidths);

                // Kopfzeile
                table.addCell("Punkt");
                table.addCell("Rückblick (r)");
                table.addCell("Vorblick (v)");
                table.addCell("Steigen (+)");
                table.addCell("Fallen (-)");

                // Tabelleninhalte
                for (int i = 0; i < rueckblickFields.size(); i++) {
                    // Prüfe, ob der erste Vorblick der Bezugspunkt ist
                    String punktName = punktFields.get(i).getText();
                    if (i == 0 && bezugspunktCheckBox.isSelected()) {
                        punktName = "BP - " + punktName; // Bezugspunkt hinzufügen
                    }

                    table.addCell(punktName);
                    table.addCell(rueckblickFields.get(i).getText());
                    table.addCell(vorblickFields.get(i).getText());
                    table.addCell(steigenLabels.get(i).getText());
                    table.addCell(fallenLabels.get(i).getText());
                }

                // Summen
                document.add(new Paragraph("Summen:"));
                document.add(new Paragraph("Σ Rückblick: " + sumRueckblickField.getText()));
                document.add(new Paragraph("Σ Vorblick: " + sumVorblickField.getText()));
                document.add(new Paragraph("Σ Steigen: " + sumSteigenField.getText()));
                document.add(new Paragraph("Σ Fallen: " + sumFallenField.getText()));
                document.add(new Paragraph("Höhenunterschied: " + sumDeltaHField.getText()));

                document.add(table);
                document.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


