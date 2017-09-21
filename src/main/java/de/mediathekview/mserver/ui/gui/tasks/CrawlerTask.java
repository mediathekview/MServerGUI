package de.mediathekview.mserver.ui.gui.tasks;

import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_CHART_ERROR;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_CHART_FINISHED;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_CHART_WORKING;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.common.util.concurrent.AtomicDouble;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mserver.crawler.CrawlerManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.ui.gui.wrappers.SenderProgressWraper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.chart.XYChart.Series;

/**
 * A task to run a specific crawler.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public class CrawlerTask extends Task<Void> {


  /**
   * A {@link SenderProgressListener} progress listener which will be called from the crawlers on
   * status updates.
   *
   * @author Nicklas Wiegandt (Nicklas2751)<br/>
   *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
   *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
   *         <b>Skype:</b> Nicklas2751<br/>
   *
   */
  class LoadListener implements SenderProgressListener {
    private final ConcurrentLinkedQueue<SenderProgressWraper> progressQue;

    public LoadListener() {
      progressQue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void updateProgess(final Sender aSender, final Progress aProgress) {
      LOG.debug(aProgress.calcProgressInPercent() + "% Progress: " + aProgress.getActualCount()
          + " of " + aProgress.getMaxCount() + " with " + aProgress.getErrorCount() + " Errors.");

      progressQue.offer(new SenderProgressWraper(aProgress, aSender));
      addNewThreadData(aSender, aProgress);
      Platform.runLater(this::updateForProgressFromQue);
    }

    private void addNewThreadData(final Sender aSender, final Progress aProgress) {
      if (aProgress.getActualCount() > 0 || aProgress.getErrorCount() > 0) {
        final String threadName = Thread.currentThread().getName();

        ConcurrentHashMap<String, AtomicLong> senderData;
        if (threadsData.containsKey(aSender)) {
          senderData = threadsData.get(aSender);
        } else {
          senderData = new ConcurrentHashMap<>();
          threadsData.put(aSender, senderData);
        }

        AtomicLong threadData;
        if (senderData.containsKey(threadName)) {
          threadData = senderData.get(threadName);
        } else {
          threadData = new AtomicLong(0);
          senderData.put(threadName, threadData);
        }
        threadData.incrementAndGet();
      }
    }

    private void updateForProgressFromQue() {
      final SenderProgressWraper progressWrapper = progressQue.poll();
      updateSenderStatistic(progressWrapper.getSender(), progressWrapper.getProgress());
      updateStatisticData();
      progressSum
          .getAndSet(progressWrapper.getProgress().calcProgressInPercent() / senderToCrawl.size());
      updateProgress(progressSum.get(), 100);

      updateThreadChart();
    }

    private void updateSenderStatistic(final Sender aSender, final Progress aProgress) {
      senderMaxCounts.get(aSender).getAndSet(aProgress.getMaxCount());
      senderErrorCounts.get(aSender).getAndSet(aProgress.getErrorCount());
      senderActualCounts.get(aSender).getAndSet(aProgress.getActualCount());
    }

    private void updateStatisticData() {
      long maxCount = 0;
      long errorCount = 0;
      long actualCount = 0;
      for (final Sender sender : senderToCrawl) {
        maxCount += senderMaxCounts.get(sender).get();
        errorCount += senderErrorCounts.get(sender).get();
        actualCount += senderActualCounts.get(sender).get();
      }
      dataError.setPieValue(errorCount);
      datafinished.setPieValue(actualCount);
      dataWorking.setPieValue((double) maxCount - actualCount);

    }

    private synchronized void updateThreadChart() {
      for (final Entry<Sender, ConcurrentHashMap<String, AtomicLong>> senderDataEntry : threadsData
          .entrySet()) {
        Series<String, Number> senderSeries;
        if (seariesUIData.containsKey(senderDataEntry.getKey())) {
          senderSeries = seariesUIData.get(senderDataEntry.getKey());
        } else {
          senderSeries =
              new Series<>(senderDataEntry.getKey().getName(), FXCollections.observableArrayList());
          seariesUIData.put(senderDataEntry.getKey(), senderSeries);
          processChartData.add(senderSeries);
        }

        for (final Entry<String, AtomicLong> progressDataEntry : senderDataEntry.getValue()
            .entrySet()) {
          ConcurrentHashMap<String, BarChart.Data<String, Number>> seriesChartData;
          if (threadsUIData.containsKey(senderSeries)) {
            seriesChartData = threadsUIData.get(senderSeries);
          } else {
            seriesChartData = new ConcurrentHashMap<>();
            threadsUIData.put(senderSeries, seriesChartData);
          }

          BarChart.Data<String, Number> threadChartData;
          if (seriesChartData.containsKey(progressDataEntry.getKey())) {
            threadChartData = seriesChartData.get(progressDataEntry.getKey());
          } else {
            threadChartData = new BarChart.Data<>(progressDataEntry.getKey(), 0l);
            seriesChartData.put(progressDataEntry.getKey(), threadChartData);
            senderSeries.getData().add(threadChartData);
          }
          threadChartData.setYValue(progressDataEntry.getValue().get());
        }
      }
    }

  }

  private static final Logger LOG = LogManager.getLogger(CrawlerTask.class);
  private final PieChart.Data dataError;
  private final PieChart.Data datafinished;
  private final PieChart.Data dataWorking;
  private final ObservableList<Data> pieChartData;
  private final ObservableList<Series<String, Number>> processChartData;

  private final AtomicDouble progressSum;

  private final ConcurrentHashMap<Sender, Series<String, Number>> seariesUIData;
  private final ConcurrentHashMap<Sender, AtomicLong> senderActualCounts;

  private final ConcurrentHashMap<Sender, AtomicLong> senderErrorCounts;
  private final ConcurrentHashMap<Sender, AtomicLong> senderMaxCounts;
  private final ObservableList<Sender> senderToCrawl;
  private final ConcurrentHashMap<Sender, ConcurrentHashMap<String, AtomicLong>> threadsData;

  private final ConcurrentHashMap<Series<String, Number>, ConcurrentHashMap<String, BarChart.Data<String, Number>>> threadsUIData;

  public CrawlerTask(final ResourceBundle aResourceBundle,
      final ObservableList<Data> aCrawlerStatisticData,
      final ObservableList<Series<String, Number>> aProcessChartData,
      final ObservableList<Sender> aSender) {
    senderToCrawl = aSender;

    pieChartData = aCrawlerStatisticData;
    dataError = new PieChart.Data(aResourceBundle.getString(BUNDLE_KEY_CHART_ERROR), 0);
    datafinished = new PieChart.Data(aResourceBundle.getString(BUNDLE_KEY_CHART_FINISHED), 0);
    dataWorking = new PieChart.Data(aResourceBundle.getString(BUNDLE_KEY_CHART_WORKING), 0);
    pieChartData.add(dataError);
    pieChartData.add(datafinished);
    pieChartData.add(dataWorking);
    pieChartData.forEach(data -> data.nameProperty()
        .bind(Bindings.concat(data.getName(), " ", data.pieValueProperty())));

    threadsData = new ConcurrentHashMap<>();
    seariesUIData = new ConcurrentHashMap<>();
    threadsUIData = new ConcurrentHashMap<>();

    processChartData = aProcessChartData;

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
  protected Void call() {
    final LoadListener listener = new LoadListener();

    final CrawlerManager crawlerManager = CrawlerManager.getInstance();
    crawlerManager.addSenderProgressListener(listener);
    crawlerManager.startCrawlerForSender(senderToCrawl.toArray(new Sender[senderToCrawl.size()]));

    crawlerManager.removeSenderProgressListener(listener);
    return null;
  }

}
