module org.homi.datastorePlugin {
	requires org.homi.plugin.api;
	requires org.homi.plugin.specification;
	requires datastore;
	
	provides org.homi.plugin.api.basicplugin.IBasicPlugin
		with org.homi.plugins.datastore.DatastorePlugin;
}