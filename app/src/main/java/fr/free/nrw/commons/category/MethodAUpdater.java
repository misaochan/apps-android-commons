package fr.free.nrw.commons.category;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.mediawiki.api.ApiResult;
import org.mediawiki.api.MWApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import fr.free.nrw.commons.CommonsApplication;

/**
 * Sends asynchronous queries to the Commons MediaWiki API to retrieve categories that are close to
 * the keyword typed in by the user. The 'srsearch' action-specific parameter is used for this
 * purpose. This class should be subclassed in CategorizationFragment.java to aggregate the results.
 */
public class MethodAUpdater extends AsyncTask<Void, Void, ArrayList<String>> {

    private String filter;
    private static final String TAG = MethodAUpdater.class.getName();
    CategorizationFragment catFragment;

    public MethodAUpdater(CategorizationFragment catFragment) {
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

    /**
     * Remove categories that contain a year in them (starting with 19__ or 20__), except for this year
     * and previous year
     * Rationale: https://github.com/commons-app/apps-android-commons/issues/47
     * @param items Unfiltered list of categories
     * @return Filtered category list
     */
    private ArrayList<String> filterYears(ArrayList<String> items) {

        Iterator<String> iterator;

        //Check for current and previous year to exclude these categories from removal
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        String yearInString = String.valueOf(year);
        Log.d(TAG, "Year: " + yearInString);

        int prevYear = year - 1;
        String prevYearInString = String.valueOf(prevYear);
        Log.d(TAG, "Previous year: " + prevYearInString);

        //Copy to Iterator to prevent ConcurrentModificationException when removing item
        for(iterator = items.iterator(); iterator.hasNext();) {
            String s = iterator.next();

            //Check if s contains a 4-digit word anywhere within the string (.* is wildcard)
            //And that s does not equal the current year or previous year
            if(s.matches(".*(19|20)\\d{2}.*") && !s.contains(yearInString) && !s.contains(prevYearInString)) {
                Log.d(TAG, "Filtering out year " + s);
                iterator.remove();
            }
        }

        Log.d(TAG, "Items: " + items.toString());
        return items;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        //If user hasn't typed anything in yet, get GPS and recent items
        if(TextUtils.isEmpty(filter)) {
            //TODO: We only want headers for this case. Maybe set Adapter to mergeAdapter in this case, otherwise set to the usual adapter
            ArrayList<String> mergedItems = new ArrayList<String>(catFragment.mergeItems());
            Log.d(TAG, "Merged items, waiting for filter");
            ArrayList<String> filteredItems = new ArrayList<String>(filterYears(mergedItems));


            return filteredItems;
        }

        //if user types in something that is in cache, return cached category
        if(catFragment.categoriesCache.containsKey(filter)) {
            ArrayList<String> cachedItems = new ArrayList<String>(catFragment.categoriesCache.get(filter));
            Log.d(TAG, "Found cache items, waiting for filter");
            ArrayList<String> filteredItems = new ArrayList<String>(filterYears(cachedItems));
            return filteredItems;
        }

        //otherwise if user has typed something in that isn't in cache, search API for matching categories
        MWApi api = CommonsApplication.createMWApi();
        ApiResult result;
        ArrayList<String> categories = new ArrayList<String>();

        //URL https://commons.wikimedia.org/w/api.php?action=query&format=xml&list=search&srwhat=text&srenablerewrites=1&srnamespace=14&srlimit=10&srsearch=
        try {
            result = api.action("query")
                    .param("format", "xml")
                    .param("list", "search")
                    .param("srwhat", "text")
                    .param("srnamespace", "14")
                    .param("srlimit", catFragment.SEARCH_CATS_LIMIT)
                    .param("srsearch", filter)
                    .get();
            Log.d(TAG, "Method A URL filter" + result.toString());
        } catch (IOException e) {
            Log.e(TAG, "IO Exception: ", e);
            //Return empty arraylist
            return categories;
        }

        ArrayList<ApiResult> categoryNodes = result.getNodes("/api/query/search/p/@title");
        for(ApiResult categoryNode: categoryNodes) {
            String cat = categoryNode.getDocument().getTextContent();
            String catString = cat.replace("Category:", "");
            categories.add(catString);
        }

        Log.d(TAG, "Found categories from Method A search, waiting for filter");
        ArrayList<String> filteredItems = new ArrayList<String>(filterYears(categories));
        return filteredItems;
    }
}
