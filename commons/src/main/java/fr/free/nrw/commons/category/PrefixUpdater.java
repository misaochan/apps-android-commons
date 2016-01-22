package fr.free.nrw.commons.category;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import org.mediawiki.api.ApiResult;
import org.mediawiki.api.MWApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.free.nrw.commons.CommonsApplication;

public class PrefixUpdater extends AsyncTask<Void, Void, Pair<String, List<String>>> {

    public AsyncResponse delegate = null;

    private String filter;
    private static final String TAG = PrefixUpdater.class.getName();
    private CategorizationFragment catFragment;

    public PrefixUpdater(CategorizationFragment catFragment) {
        this.catFragment = catFragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        filter = catFragment.categoriesFilter.getText().toString();
        catFragment.categoriesSearchInProgress.setVisibility(View.VISIBLE);
        catFragment.categoriesNotFoundView.setVisibility(View.GONE);

        catFragment.categoriesSkip.setVisibility(View.GONE);
    }

    @Override
    protected void onPostExecute(Pair<String,List<String>> pair) {
        super.onPostExecute(pair);
        String type = pair.first;
        List<String> categories = pair.second;
        catFragment.setCatsAfterAsync(categories, filter);
        //TODO: Return its own List
        delegate.processFinish(type, categories);


    }

    @Override
    protected Pair<String, List<String>> doInBackground(Void... voids) {
        //If user hasn't typed anything in yet, get GPS and recent items
        if(TextUtils.isEmpty(filter)) {
            String type = "empty";
            Pair pair = Pair.create(type, catFragment.recentCatQuery());
            return pair;
        }

        //if user types in something that is in cache, return cached category
        if(catFragment.categoriesCache.containsKey(filter)) {
            String type = "cache";
            Pair pair = Pair.create(type, catFragment.categoriesCache.get(filter));
            return pair;
        }

        //otherwise if user has typed something in that isn't in cache, search API for matching categories
        MWApi api = CommonsApplication.createMWApi();
        ApiResult result;
        ArrayList<String> categories = new ArrayList<String>();
        try {
            result = api.action("query")
                    .param("list", "allcategories")
                    .param("acprefix", filter)
                    .param("aclimit", catFragment.SEARCH_CATS_LIMIT)
                    .get();
            Log.d(TAG, "Prefix URL filter" + result.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ArrayList<ApiResult> categoryNodes = result.getNodes("/api/query/allcategories/c");
        for(ApiResult categoryNode: categoryNodes) {
            categories.add(categoryNode.getDocument().getTextContent());
        }

        catFragment.categoriesCache.put(filter, categories);

        String type = "filter";
        Pair pair = Pair.create(type, categories);
        return pair;

    }
}
