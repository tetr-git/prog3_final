package ui.cli.parser;

import domain_logic.MediaFileRepoList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import routing.handler.EventHandler;
import routing.listener.*;
import ui.cli.ConsoleManagement;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.*;

class ParseReadTest {

    MediaFileRepoList mediaFileRepoList;
    EventHandler inputHandler;
    EventHandler outputHandler;
    EventHandler outputHandlerDump;
    ConsoleManagement consoleManagement;
    ParseCreate parseCreate;
    ParseRead parseRead;
    SimpleDateFormat sdf;
    Date todayAsDate;

    @BeforeEach
    void setUp() {
        mediaFileRepoList = new MediaFileRepoList(new BigDecimal(1000000000));
        inputHandler = new EventHandler();
        outputHandler = new EventHandler();
        outputHandlerDump = new EventHandler();
        inputHandler.add(new CreateMediaListener(mediaFileRepoList, outputHandlerDump));
        inputHandler.add(new CreateUploaderListener(mediaFileRepoList,outputHandlerDump));
        inputHandler.add(new ReadMediaListener(mediaFileRepoList,outputHandler));
        inputHandler.add(new ReadUploaderListener(mediaFileRepoList,outputHandler));
        inputHandler.add(new ReadTagListener(mediaFileRepoList,outputHandler));
        consoleManagement = mock(ConsoleManagement.class);
        outputHandler.add(new CliOutputListener(consoleManagement));

        parseCreate = new ParseCreate(inputHandler);
        parseRead = new ParseRead(inputHandler);

        //get date from today
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar today = Calendar.getInstance();
        todayAsDate = today.getTime();
    }

    /*
    Test with standard repository active (repository number )
    todo check text output with one or more repositories
     */

    @Test
    void ReadUploader() {

        EventHandler i = new EventHandler();
        EventHandler o = new EventHandler();
        i.add(new CreateUploaderListener(mediaFileRepoList,o));
        ConsoleManagement c = mock(ConsoleManagement.class);
        o.add(new CliOutputListener(c));

        ParseCreate parseCreateUploader = new ParseCreate(i);

        parseCreateUploader.execute("Produzent1");

        verify(c).writeToConsole("Repository: 0\n" +
                "added uploader Produzent1");
    }

    @Test
    void ReadMedia() {
        parseCreate.execute("Produzent1");
        parseCreate.execute("InteractiveVideo Produzent1 Lifestyle,News 500 360");

        parseRead.execute("content");

        verify(consoleManagement, times(1)).writeToConsole("Repository: 0\n" +
                "1\tInteractiveVideo\tProduzent1\t[Lifestyle, News]\t0\t500.0\tPT8M20S\t250000.0\t"+sdf.format(todayAsDate)+"\t\t0\n");

    }

    @Test
    void ReadMediaFilteredByType() {
        parseCreate.execute("Hans");
        parseCreate.execute("Bert");
        parseCreate.execute("audioVideo Hans Lifestyle,News 500 360");
        parseCreate.execute("licensedAudio Bert Lifestyle 500 360");
        parseCreate.execute("audio Hans Lifestyle,News 500 360");

        parseRead.execute("content audio");

        verify(consoleManagement).writeToConsole("Repository: 0\n" +
                "3\tAudio\tHans\t[Lifestyle, News]\t0\t500.0\tPT8M20S\t250000.0\t"+sdf.format(todayAsDate)+"\t0\n");
    }

    @Test
    void ReadExistingTags() {

        parseCreate.execute("Hans");
        parseCreate.execute("audioVideo Hans Lifestyle,News 500 360");
        parseCreate.execute("licensedAudio Bert Lifestyle 500 360");
        parseCreate.execute("audio Hans Lifestyle,News 500 360");

        parseRead.execute("tag i");

        verify(consoleManagement).writeToConsole("Repository: 0\n" +
                "Lifestyle\tNews\t");
    }

    @Test
    void ReadNotExistingTags() {

        parseCreate.execute("Hans");
        parseCreate.execute("audioVideo Hans Lifestyle,News 500 360");
        parseCreate.execute("licensedAudio Bert Lifestyle 500 360");
        parseCreate.execute("audio Hans Lifestyle,News 500 360");

        parseRead.execute("tag e");

        verify(consoleManagement).writeToConsole("Repository: 0\n" +
                "Animal\tTutorial\t");
    }
}