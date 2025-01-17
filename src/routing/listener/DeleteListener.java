package routing.listener;

import domain_logic.MediaFileRepoList;
import domain_logic.MediaFileRepository;
import routing.events.DeleteEvent;
import routing.events.OutputEvent;
import routing.handler.EventHandler;

import java.util.EventObject;

public class DeleteListener implements EventListener {
    private final MediaFileRepoList mediaFileRepoList;
    private final EventHandler outputHandler;
    private String[] arg;
    private EventObject event;

    public DeleteListener(MediaFileRepoList mediaFileRepoList, EventHandler outputHandler) {
        this.mediaFileRepoList = mediaFileRepoList;
        this.outputHandler = outputHandler;
    }

    @Override
    public void onEvent(EventObject eventObject) {
        if (eventObject.toString().equals("DeleteEvent")) {
            event = eventObject;
            for (MediaFileRepository repository : mediaFileRepoList.getRepoList()) {
                if (repository.isActiveRepository()) {
                    this.execute(repository);
                }
            }
        }
    }

    private void execute(MediaFileRepository mR) {
        String response = "";
        if (mR.deleteUploader(((DeleteEvent) event).getDeleteString())) {
            response += "Uploader deleted";
        } else if (mR.deleteMediaFiles(((DeleteEvent) event).getDeleteString())) {
            response += "Media deleted";
        } else {
            response = "nothing deleted";
        }
        OutputEvent outputEvent;
        outputEvent = new OutputEvent(event, response);
        outputHandler.handle(outputEvent);
    }
}
