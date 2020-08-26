/*
 * Get the Definitional Elements collection for the Substance -- which comes from a class-specific routine
 * and create a hash that will enable (Lucene) searching.
 */
package ix.ginas.indexers;

import ix.core.models.DefinitionalElements;
import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.core.util.LogUtil;
import ix.ginas.models.v1.Substance;
import java.util.List;
import java.util.function.Consumer;
import play.Logger;

/**
 *
 * @author Mitch Miller
 */
public class SubstanceDefinitionalHashIndexer implements IndexValueMaker<Substance>
{

	@Override
	public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer)
	{
		LogUtil.trace(()->String.format("Starting in SubstanceDefinitionalHashIndexer.createIndexableValues. class: %s ",
						substance.getClass().getName()));
		try
		{
			Logger.trace("about to call substance.getDefinitionalElements");
			DefinitionalElements elements = substance.getDefinitionalElements();
			LogUtil.trace(()->String.format(" received %d elements", elements.getElements().size()));
			if( elements==null)
			{
				Logger.trace("elements null");
				return;
			}
			if( elements.getElements().isEmpty()) 
			{
				Logger.trace("elements empty");
				return;
			}
			
			List<String> layerHashes = elements.getDefinitionalHashLayers();
			LogUtil.trace(()->String.format(" %d layers", layerHashes.size()));
			for (int layer = 1; layer <= layerHashes.size(); layer++)
			{
				String layerName = "root_definitional_hash_layer_" + layer;
				LogUtil.trace(()->"layerName: " + layerName);
				consumer.accept(IndexableValue.simpleStringValue(layerName, layerHashes.get(layer - 1)));
			}
		} catch (Exception ex)
		{
			Logger.error("Error during indexing", ex);
			ex.printStackTrace();
		}
	}

}
