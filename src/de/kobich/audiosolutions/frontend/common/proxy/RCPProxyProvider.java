package de.kobich.audiosolutions.frontend.common.proxy;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import de.kobich.commons.net.IProxyProvider;
import de.kobich.commons.net.Proxy;

/**
 * RCP proxy provider.
 * 
 */
public class RCPProxyProvider implements IProxyProvider {
	private static final Logger logger = Logger.getLogger(RCPProxyProvider.class);

	@Override
	public void init() {
	}

	@Override
	public Proxy getProxy(URL url) {
		ServiceTracker<IProxyService, IProxyService> proxyTracker = new ServiceTracker<IProxyService, IProxyService>(FrameworkUtil.getBundle(
				this.getClass()).getBundleContext(), IProxyService.class.getName(), null);
		proxyTracker.open();
		IProxyService proxyService = proxyTracker.getService();
		try {
			if (proxyService != null && proxyService.isProxiesEnabled()) {
				IProxyData[] proxyDatas = proxyService.select(url.toURI());
				if (proxyDatas.length > 0) {
					IProxyData proxyData = proxyDatas[0];
					if (proxyData.getHost() != null) {
						logger.info("Using proxy: " + proxyData.getHost());
						return new Proxy(proxyData.getHost(), proxyData.getPort(), proxyData.getUserId(), proxyData.getPassword());
					}
				}
			}
		}
		catch (URISyntaxException exc) {
			logger.warn("Converting url to uri failed", exc);
		}
		finally {
			proxyTracker.close();
		}
		return null;
	}

	@Override
	public void dispose() {
	}

}
