/* Copyright 2010 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.engine;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.isencia.passerelle.ext.TypeConverterProvider;
import com.isencia.passerelle.message.type.TypeConversionChain;


public class Activator implements BundleActivator {
	
	private static Activator instance;
	private TypeConverterProviderSvcTracker typeCvtSvcTracker;

	public void start(BundleContext context) throws Exception {
		Activator.instance = this;
		typeCvtSvcTracker = new TypeConverterProviderSvcTracker(context);
		typeCvtSvcTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		typeCvtSvcTracker.close();
		Activator.instance=null;
	}
	
	public static Activator getInstance() {
		return instance;
	}
	
	static class TypeConverterProviderSvcTracker extends ServiceTracker {
		public TypeConverterProviderSvcTracker(BundleContext context) {
			super(context, TypeConverterProvider.class.getName(), null);
		}

		@Override
		public TypeConverterProvider addingService(ServiceReference reference) {
			TypeConverterProvider provider = (TypeConverterProvider) super.addingService(reference);
			TypeConversionChain.getInstance().setConverterProvider(provider);
			return provider;
		}

		@Override
		public void modifiedService(ServiceReference reference, Object service) {
			super.modifiedService(reference, service);
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
			super.removedService(reference, service);
			TypeConversionChain.getInstance().setConverterProvider(null);
		}
	}
}
