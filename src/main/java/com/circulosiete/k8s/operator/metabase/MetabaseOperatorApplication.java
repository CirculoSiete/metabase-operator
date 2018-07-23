package com.circulosiete.k8s.operator.metabase;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MetabaseOperatorApplication {

  public static void main(String[] args) throws InterruptedException {
    final CountDownLatch closeLatch = new CountDownLatch(1);
    Config config = Config.autoConfigure(null);

    try (final KubernetesClient client = new DefaultKubernetesClient(config)) {

      try (Watch watch = client.configMaps().inAnyNamespace().watch(new Watcher<ConfigMap>() {
        @Override
        public void eventReceived(Action action, ConfigMap resource) {
          //log.info("{}: {}", action, resource.getMetadata().getResourceVersion());
          String s = resource.getMetadata().getNamespace() + ":" + resource.getMetadata().getName();
          System.out.println("CM: " + s);
          System.out.println("action " + action);
        }

        @Override
        public void onClose(KubernetesClientException cause) {
          log.debug("Watcher onClose");
          if (cause != null) {
            log.error(cause.getMessage(), cause);
            System.err.println(cause.getMessage());
            closeLatch.countDown();
          }
        }
      })) {
        closeLatch.await(60, TimeUnit.SECONDS);
      } catch (KubernetesClientException | InterruptedException e) {
        log.error("Could not watch resources", e);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e.getMessage(), e);

      Throwable[] suppressed = e.getSuppressed();
      if (suppressed != null) {
        for (Throwable t : suppressed) {
          log.error(t.getMessage(), t);
        }
      }
    }

    Thread.sleep(60000l);
  }
}
