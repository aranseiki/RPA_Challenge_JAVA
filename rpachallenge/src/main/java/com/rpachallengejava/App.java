package com.rpachallengejava;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class App {
    public static WebDriver navegador;
    public static EdgeOptions optionsDriver;
    public static final String PASTA_DOWNLOAD = String.join("\\", Paths.get("").toAbsolutePath().toString(), "assets");
    public static String caminhoArquivoExcela = "";
    public static List<List<String>> conteudoArquivoExcela = new ArrayList<>();

    public static void main( String[] args ) {
        criarPasta(new File(PASTA_DOWNLOAD));
        iniciarNavegador(
            "https://rpachallenge.com/"
        );

        boolean validacaoArquivoExcel = baixarArquivoDados();

        if (validacaoArquivoExcel == true) {
            lerArquivoExcel(caminhoArquivoExcela, conteudoArquivoExcela);
        }

        boolean validacaoInicio = iniciarDesafio();
        if (validacaoInicio == true & validacaoArquivoExcel == true) {
            preencherFormulario();

            exibirResultadoNaTela();
        }

        encerrarNavegador();
    }


    public static boolean criarPasta(File ObjetoPasta) {
        boolean validacaoPastaDownload = false;
        int contagem = 0;
        while (validacaoPastaDownload == false & contagem < 30) {
            try {
                validacaoPastaDownload = ObjetoPasta.mkdirs();

                if(validacaoPastaDownload == true) {
                    return validacaoPastaDownload;
                }
            } catch (Exception erro) {
                validacaoPastaDownload = false;
            }
            
            contagem = contagem + 1;
        }

        return validacaoPastaDownload;
    }


    public static boolean iniciarNavegador( String url ) {
        optionsDriver = new EdgeOptions();
        optionsDriver.addArguments("--disable-dev-shm-usage");
        optionsDriver.addArguments(
            "--download.default_directory="
            + PASTA_DOWNLOAD
        );
        navegador = new EdgeDriver(optionsDriver);

        navegador.manage().deleteAllCookies();
        navegador.manage().window().maximize();

        navegador.get(url);

        return true;
    }


    public static boolean baixarArquivoDados() {
        boolean validacaoBotaoDownloadExcel = false;
        int contagem = 0;

        String urlArquivoExcel = "";
        while (validacaoBotaoDownloadExcel == false & contagem < 30) {
            try {
                urlArquivoExcel = navegador.findElement(By.xpath("//a[@href='./assets/downloadFiles/challenge.xlsx']")).getAttribute("href");
                validacaoBotaoDownloadExcel = true;
            } catch (Exception erro) {
                validacaoBotaoDownloadExcel = false;
            }

            contagem = contagem + 1;
        }

        try {
            URL url = new URL(urlArquivoExcel);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestMethod("GET");

            String nomeArquivoExcel = url.getFile().split("/")[url.getFile().split("/").length-1];
            caminhoArquivoExcela = String.join("\\", PASTA_DOWNLOAD, nomeArquivoExcel);

            // Object qualquer = con.getContent();
            InputStream inputStream = con.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            FileOutputStream outputStream = new FileOutputStream(caminhoArquivoExcela);
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
        } catch (Exception erro) {
            erro.printStackTrace();

            return false;
        }

        return true;
    }


    public static boolean iniciarDesafio() {
        WebElement buttonStart = navegador.findElement(By.xpath("//button[text()='Start']"));
        buttonStart.click();

        boolean validacaoBotaoStart = false;
        int contagem = 0;

        String valorBotaoStart = "";
        while (validacaoBotaoStart == false & contagem < 30) {
            try {
                valorBotaoStart = navegador.findElement(By.xpath("//button[text()='Round 1']")).getText();
                if (valorBotaoStart.toUpperCase().equals("ROUND 1")) {
                    validacaoBotaoStart = true;
                }
            } catch (Exception erro) {
                validacaoBotaoStart = false;
            }

            contagem = contagem + 1;
        }

        return true;
    }


    public static void preencherFormulario() {
        for (List<String> linhaObject : conteudoArquivoExcela) {
            if(linhaObject.get(0).toUpperCase().equals("FIRST NAME")) {
                continue;
            }

            navegador.findElement(By.xpath("//label[text()='First Name']/following-sibling::input")).sendKeys(linhaObject.get(0));
            navegador.findElement(By.xpath("//label[text()='Last Name']/following-sibling::input")).sendKeys(linhaObject.get(1));
            navegador.findElement(By.xpath("//label[text()='Company Name']/following-sibling::input")).sendKeys(linhaObject.get(2));
            navegador.findElement(By.xpath("//label[text()='Role in Company']/following-sibling::input")).sendKeys(linhaObject.get(3));
            navegador.findElement(By.xpath("//label[text()='Address']/following-sibling::input")).sendKeys(linhaObject.get(4));
            navegador.findElement(By.xpath("//label[text()='Email']/following-sibling::input")).sendKeys(linhaObject.get(5));
            navegador.findElement(By.xpath("//label[text()='Phone Number']/following-sibling::input")).sendKeys(linhaObject.get(6));
            
            navegador.findElement(By.xpath("//input[@value='Submit']")).click();
        }
    }


    public static void lerArquivoExcel(String caminhoArquivoExcela, List<List<String>> conteudoArquivoExcela) {
        try {
            FileInputStream file = new FileInputStream(caminhoArquivoExcela);

            // Crie um objeto Workbook para representar o arquivo .xlsx
            Workbook workbook = new XSSFWorkbook(file);

            // Obtenha a primeira planilha do arquivo
            Sheet sheet = workbook.getSheetAt(0);

            // Percorra as linhas da planilha
            for (Row row : sheet) {
                List<String> rowValues = new ArrayList<>();

                // Percorra as células de cada linha
                for (Cell cell : row) {
                    // Obtenha o valor da célula e adicione à lista
                    CellType cellType = cell.getCellType();
                    if (cellType == CellType.BLANK) {
                        continue;
                    } else {
                        String cellValue = cell.toString();
                        rowValues.add(cellValue);
                    }
                }

                if (rowValues.isEmpty() == false) {
                    conteudoArquivoExcela.add(rowValues);
                }
            }

            // Feche o arquivo
            workbook.close();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean exibirResultadoNaTela() {
        String mensagem1 = navegador.findElement(By.xpath("(//div[contains(@class, 'congratulations')]/div)[1]")).getText();
        String mensagem2 = navegador.findElement(By.xpath("(//div[contains(@class, 'congratulations')]/div)[2]")).getText();

        System.out.println(mensagem1);
        System.out.println(mensagem2);
        return true;
    }


    public static boolean encerrarNavegador() {
        navegador.quit();
        return true;
    }
}
