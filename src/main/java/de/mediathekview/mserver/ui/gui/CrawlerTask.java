package de.mediathekview.mserver.ui.gui;

import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AtomicDouble;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mserver.crawler.CrawlerManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;

public class CrawlerTask extends Task<Void>
{
    private final ObservableList<Sender> senderToCrawl;
    private final AtomicDouble progressSum;
    private final ObservableList<Data> pieChartData;
    private final PieChart.Data dataError;
    private final PieChart.Data datafinished;
    private final PieChart.Data dataWorking;

    private final ConcurrentHashMap<Sender, AtomicLong> senderMaxCounts;
    private final ConcurrentHashMap<Sender, AtomicLong> senderActualCounts;
    private final ConcurrentHashMap<Sender, AtomicLong> senderErrorCounts;

    public CrawlerTask(final ResourceBundle aResourceBundle, final ObservableList<Data> aCrawlerStatisticData,
            final ObservableList<Sender> aSender)
    {
        senderToCrawl = aSender;
        pieChartData = aCrawlerStatisticData;
        dataError = new PieChart.Data(aResourceBundle.getString("chart.error"), 0);
        datafinished = new PieChart.Data(aResourceBundle.getString("chart.finished"), 0);
        dataWorking = new PieChart.Data(aResourceBundle.getString("chart.working"), 0);
        pieChartData.add(dataError);
        pieChartData.add(datafinished);
        pieChartData.add(dataWorking);

        pieChartData.forEach(
                data -> data.nameProperty().bind(Bindings.concat(data.getName(), " ", data.pieValueProperty())));

        progressSum = new AtomicDouble(0);
        senderMaxCounts = new ConcurrentHashMap<>();
        senderActualCounts = new ConcurrentHashMap<>();
        senderErrorCounts = new ConcurrentHashMap<>();
        senderToCrawl.forEach(s -> {
            senderMaxCounts.put(s, new AtomicLong(0));
            senderActualCounts.put(s, new AtomicLong(0));
            senderErrorCounts.put(s, new AtomicLong(0));
        });
    }

    @Override
    protected Void call()
    {
        final LoadListener listener = new LoadListener();

        final CrawlerManager crawlerManager = CrawlerManager.getInstance();
        crawlerManager.addSenderProgressListener(listener);
        crawlerManager.startCrawlerForSender(senderToCrawl.toArray(new Sender[senderToCrawl.size()]));

        crawlerManager.removeSenderProgressListener(listener);
        return null;
    }

    class LoadListener implements SenderProgressListener
    {

        @Override
        public void updateProgess(final Sender aSender, final Progress aProgress)
        {
            Platform.runLater(() -> {
                updateSenderStatistic(aSender, aProgress);
                updateStatisticData();
                progressSum.getAndSet(aProgress.calcProgressInPercent() / senderToCrawl.size());
                updateProgress(progressSum.get(), 100);
            });
        }

        private void updateStatisticData()
        {
            long maxCount = 0;
            long errorCount = 0;
            long actualCount = 0;
            for (final Sender sender : senderToCrawl)
            {
                maxCount += senderMaxCounts.get(sender).get();
                errorCount += senderErrorCounts.get(sender).get();
                actualCount += senderActualCounts.get(sender).get();
            }
            dataError.setPieValue(errorCount);
            datafinished.setPieValue(actualCount);
            dataWorking.setPieValue(maxCount - actualCount);

        }

        private void updateSenderStatistic(final Sender aSender, final Progress aProgress)
        {
            senderMaxCounts.get(aSender).getAndSet(aProgress.getMaxCount());
            senderErrorCounts.get(aSender).getAndSet(aProgress.getErrorCount());
            senderActualCounts.get(aSender).getAndSet(aProgress.getActualCount());
        }

    }

}
