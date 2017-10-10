/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;


import com.adobe.internal.io.ByteWriter;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveFullOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveOptions;
import com.adobe.pdfjt.services.pdfa.PDFA1bConfiguredConversionHandler;
import com.adobe.pdfjt.services.pdfa.PDFAConformanceLevel;
import com.adobe.pdfjt.services.pdfa.PDFAConversionOptions;
import com.adobe.pdfjt.services.pdfa.PDFAConversionOptionsFactory;
import com.adobe.pdfjt.services.pdfa.PDFAService;

import com.datalogics.pdf.document.FontSetLoader;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;


/**
 * This sample demonstrates how to convert a PDF to PDF/A-1b archive file.
 * <p>
 * To be certified as a PDF/A1 document, the PDF cannot contain a transparency, 
 * though PDF/A2 documents do support transparencies. PDF Java Toolkit is not 
 * designed to work with transparencies in PDF documents. But the PDF Java 
 * Toolkit Core API is designed to report an error if a problem appears when 
 * attempting to convert a document to PDF/A. 
 * </p>
 */

public final class ConvertPdfDocument {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String INPUT_UNCONVERTED_PDF_PATH = "UnConvertedPdf.pdf";
    public static final String OUTPUT_CONVERTED_PDF_PATH = "ConvertedPdfa-1b.pdf";


    /**
     * This is a utility class, and won't be instantiated.
     */
    private ConvertPdfDocument() {}

    /**
     * Main program.
     *
     * @param args command line arguments
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        final URL inputUrl = ConvertPdfDocument.class.getResource(INPUT_UNCONVERTED_PDF_PATH);
        URL outputUrl = null;
        if (args.length > 0) {
            outputUrl = IoUtils.createUrlFromPath(args[0]);
        } else {
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_CONVERTED_PDF_PATH);
        }

        convertToPdfA1B(inputUrl, outputUrl);
    }

    /**
     * Converts an input PDF document to the PDF/A-1b standard.
     *
     * @param inputUrl The URL of the document to be converted
     * @param outputUrl The URL of the converted document
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     */
    public static void convertToPdfA1B(final URL inputUrl, final URL outputUrl)
                    throws IOException, PDFInvalidDocumentException, PDFIOException, PDFSecurityException,
                    PDFInvalidParameterException, PDFUnableToCompleteOperationException {
        ByteWriter writer = null;
        // Attach font set to PDF
        final PDFFontSet pdfaFontSet = FontSetLoader.newInstance().getFontSet();
        final PDFOpenOptions openOptions = PDFOpenOptions.newInstance();
        openOptions.setFontSet(pdfaFontSet);

        final PDFDocument pdfDoc = DocumentUtils.openPdfDocumentWithOptions(inputUrl, openOptions);

        final PDFA1bConfiguredConversionHandler handler = new PDFA1bConfiguredConversionHandler();
        final PDFAConversionOptions options = PDFAConversionOptionsFactory.getConfiguredPdfA1bInstance(pdfDoc);

        try {
            // Attempt to convert the PDF to PDF/A-1b
            if (PDFAService.convert(pdfDoc, PDFAConformanceLevel.Level_1b, options, handler)) {
                final PDFSaveOptions saveOpt = PDFSaveFullOptions.newInstance();

                // If the pdf contains compressed object streams, we should
                // decompress these so that the pdf can be converted to PDF/A-1b
                if (handler.requiresObjectDecompression()) {
                    saveOpt.setObjectCompressionMode(PDFSaveOptions.OBJECT_COMPRESSION_NONE);
                }

                writer = IoUtils.newByteWriter(outputUrl);
                pdfDoc.save(writer, saveOpt);

                final String successMsg = "\nConverted output written to: " + outputUrl.toString();
                LOGGER.info(successMsg);
            } else {
                LOGGER.info("Errors encountered when converting document.");
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }


}
