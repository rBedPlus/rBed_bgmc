package chainHash.movie.manager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import chainHash.dsa.list.List;
import chainHash.dsa.sorter.Sorter;
import chainHash.movie.data.Movie;
import chainHash.movie.data.WatchRecord;
import chainHash.movie.factory.DSAFactory;
import chainHash.movie.io.InputFileReader;
import net.datastructures.Map;

/**
 * Movie Manager
 *
 * @author Eason Li
 *
 */
public class MovieManager {

    /**
     * movie list
     */
    private final List<Movie>       movielist;
    /**
     * watch list
     */
    private final List<WatchRecord> watchlist;

    /**
     * Creates a MovieManager instance for handling utility functions
     * 
     * @param pathToMovies
     *            the path to the file of movie records
     * @param pathToHistory
     *            the path to the file of watch records
     * @throws ParseException
     *             if the watch history file has incorrectly formatted date
     *             information
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public MovieManager ( String pathToMovies, String pathToHistory )
            throws ParseException, UnsupportedEncodingException, IOException {
        movielist = InputFileReader.readMovieFile( pathToMovies );
        watchlist = InputFileReader.readHistoryFile( pathToHistory );
    }

    /**
     * Returns a list of watch records associated with the requested movie title
     * 
     * @param title
     *            the title of the movie for which to retrieve watch record
     *            information
     * @return a list of watch records associated with the requested movie title
     */
    public List<WatchRecord> getWatchHistory ( String title ) {

        String tempID = null;
        for ( int i = 0; i < movielist.size(); i++ ) {
            if ( movielist.get( i ).getTitle().equals( title ) ) {
                tempID = movielist.get( i ).getId();
                break;
            }
        }
        if ( tempID == null ) {
            return DSAFactory.getIndexedList();
        }
        final List<WatchRecord> list = DSAFactory.getIndexedList();
        for ( int i = 0; i < watchlist.size(); i++ ) {
            if ( watchlist.get( i ).getMovieId().equals( tempID ) ) {
                list.addLast( watchlist.get( i ) );
            }
        }
        if ( list.size() == 0 ) {
            return DSAFactory.getIndexedList();
        }
        final WatchRecord[] arr = new WatchRecord[list.size()];
        for ( int i = 0; i < list.size(); i++ ) {
            arr[i] = list.get( i );
        }
        final Sorter<WatchRecord> sorter = DSAFactory.getComparisonSorter();
        sorter.sort( arr );
        final List<WatchRecord> wlist = DSAFactory.getIndexedList();
        for ( int i = arr.length - 1; i >= 0; i-- ) {
            wlist.addLast( arr[i] );
        }
        return wlist;
    }

    /**
     * Return a list of movie records that contains the top n most frequently
     * watched movies
     * 
     * @param numberOfMovies
     *            the n most frequently watched movies to include in the list
     * @return a list of movie records that contains the top n most frequently
     *         watched movies
     */
    public List<Movie> getMostFrequentlyWatchedMovies ( int numberOfMovies ) {

        if ( watchlist.size() == 0 || numberOfMovies <= 0 ) {
            return DSAFactory.getIndexedList();
        }

        final Map<String, Integer> wmap = DSAFactory.getMap();
        for ( int i = 0; i < watchlist.size(); i++ ) {
            final WatchRecord r = watchlist.get( i );
            final String id = r.getMovieId();
            if ( wmap.get( id ) != null ) {
                wmap.put( id, wmap.get( id ) + 1 );
            }
            else {
                wmap.put( id, 1 );
            }
        }

        final List<MovieEntry> llist = DSAFactory.getIndexedList();
        for ( int i = 0; i < movielist.size(); i++ ) {
            final Movie movie = movielist.get( i );
            if ( wmap.get( movie.getId() ) != null ) {
                final MovieEntry m = new MovieEntry( movie, wmap.get( movie.getId() ) );
                llist.addLast( m );
            }
        }

        final MovieEntry[] entries = new MovieEntry[llist.size()];

        for ( int i = 0; i < llist.size(); i++ ) {
            entries[i] = llist.get( i );
        }

        final Sorter<MovieEntry> sorter = DSAFactory.getComparisonSorter();

        sorter.sort( entries );

        final List<Movie> list = DSAFactory.getIndexedList();
        for ( int i = 0; i < entries.length; i++ ) {
            if ( i == numberOfMovies ) {
                break;
            }
            list.addLast( entries[i].getMovie() );
        }
        return list;

    }

    /**
     * Return a list of movie records that have been watched less than a
     * specific threshold percentage
     * 
     * @param threshold
     *            the percentages threshold to use, as a whole number
     * @return a list of movie records that have been watched less than the
     *         specified threshold percentage
     */
    public List<Movie> getMoviesByWatchDuration ( int threshold ) {

        if ( watchlist.size() == 0 ) {
            return DSAFactory.getIndexedList();
        }
        final Map<String, Integer> map = DSAFactory.getMap();
        for ( int i = 0; i < watchlist.size(); i++ ) {
            final WatchRecord r = watchlist.get( i );
            final String id = r.getMovieId();
            final int watchtime = r.getWatchTime();
            if ( map.get( id ) != null ) {
                if ( map.get( id ) < watchtime ) {
                    // map.remove(id);
                    map.put( id, watchtime );
                }
            }
            else {
                map.put( id, watchtime );
            }
        }

        final List<MovieEntry> entrylist = DSAFactory.getIndexedList();
        for ( int i = 0; i < movielist.size(); i++ ) {
            final Movie movie = movielist.get( i );
            if ( map.get( movie.getId() ) != null ) {
                final int percent = map.get( movie.getId() ) * 100 / movie.getRuntime();
                if ( percent < threshold ) {
                    final MovieEntry e = new MovieEntry( movie, percent );
                    entrylist.addLast( e );
                }
            }
        }
        final MovieEntry[] entries = new MovieEntry[entrylist.size()];
        // int count = 0;
        for ( int i = 0; i < entrylist.size(); i++ ) {
            entries[i] = entrylist.get( i );
        }

        final Sorter<MovieEntry> sorter = DSAFactory.getComparisonSorter();
        sorter.sort( entries );

        final List<Movie> list = DSAFactory.getIndexedList();
        for ( int i = 0; i < entries.length; i++ ) {
            list.addLast( entries[i].getMovie() );
        }
        return list;

    }

    /**
     * Movie Entry
     *
     * @author Eason Li
     *
     */
    private class MovieEntry implements Comparable<MovieEntry> {
        /**
         * movie
         */
        private Movie   movie;
        /**
         * value
         */
        private Integer value;
        /**
         * id
         */
        private String  id;

        // public MovieEntry(Entry<Movie, Integer> entry) {
        // this.setMovie(entry.getKey());
        // this.setValue(entry.getValue());
        // }

        /**
         * movie entry constructor
         *
         * @param movie
         *            movie
         * @param value
         *            value
         */
        public MovieEntry ( Movie movie, Integer value ) {
            this.setMovie( movie );
            this.setValue( value );
            this.setId( movie.getId() );
        }

        /**
         * get value
         *
         * @return int value
         */
        public Integer getValue () {
            return value;
        }

        /**
         * set value
         *
         * @param value
         *            set value
         */
        public void setValue ( Integer value ) {
            this.value = value;
        }

        /**
         * get movie
         *
         * @return movie
         */
        public Movie getMovie () {
            return movie;
        }

        /**
         * set movie
         *
         * @param movie
         *            movie
         */
        public void setMovie ( Movie movie ) {
            this.movie = movie;
        }

        /**
         * get id
         *
         * @return id
         */
        public String getId () {
            return id;
        }

        /**
         * set id
         *
         * @param id
         *            id
         */
        public void setId ( String id ) {
            this.id = id;
        }

        /**
         * compare to method
         *
         * @param o
         *            movie entry
         * @return integer to show the comparison
         */
        @Override
        public int compareTo ( MovieEntry o ) {
            if ( this.getValue() > o.getValue() ) {
                return -1;
            }
            else if ( this.getValue() < o.getValue() ) {
                return 1;
            }
            else {
                if ( this.getMovie().getTitle().compareTo( o.getMovie().getTitle() ) < 0 ) {
                    return -1;
                }
                else if ( this.getMovie().getTitle().compareTo( o.getMovie().getTitle() ) > 0 ) {
                    return 1;
                }
                else {
                    if ( this.getId().compareTo( o.getId() ) < 0 ) {
                        return -1;
                    }
                    else if ( this.getId().compareTo( o.getId() ) > 0 ) {
                        return 1;
                    }
                    else {
                        return 0;
                    }
                }
            }

        }

    }

}
