package de.embl.cba.platynereis.ui;

import bdv.util.Bdv;
import de.embl.cba.platynereis.Constants;
import de.embl.cba.platynereis.MainCommand;
import de.embl.cba.platynereis.PlatynereisDataSource;
import de.embl.cba.platynereis.Utils;
import ij.gui.GenericDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static de.embl.cba.platynereis.Utils.asArgbType;

public class BdvSourcesPanel extends JPanel implements ActionListener
{
    public static final String CHANGE_COLOR = "Change color";
    public static final String ADAPT_BRIGHTNESS = "Adapt brightness";
    public static final String REMOVE = "Remove";
    public static final String CANCELLED = "Cancelled";
    public static final String COLOR_ACTION = "C___";
    public static final String BRIGHTNESS_ACTION = "B___";
    public static final String TOGGLE_ACTION = "T___";
    public static final String REMOVE_ACTION = "X___";

    public ArrayList< Color > colors;

    protected Map< String, JPanel > panels;
    JFrame frame;
    final MainFrame mainFrame;
    final MainCommand mainCommand;
    final Bdv bdv;
    private final Map< String, PlatynereisDataSource > dataSources;


    public BdvSourcesPanel( MainFrame mainFrame, Bdv bdv, MainCommand mainCommand )
    {
        this.mainFrame = mainFrame;
        this.bdv = bdv;
        this.mainCommand = mainCommand;
        this.dataSources = mainCommand.dataSources;
        this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS ) );
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        panels = new LinkedHashMap<>(  );
        this.addSource( dataSources.get( mainCommand.getEmRawDataName() ).name );
        initColors();
    }

    private void initColors()
    {
        colors = new ArrayList<>(  );

        colors.add( Color.YELLOW );
        colors.add( Color.MAGENTA );
        colors.add( Color.CYAN );
        colors.add( Color.BLUE );
        colors.add( Color.ORANGE );
        colors.add( Color.GREEN );
        colors.add( Color.PINK );

    }

    private Color getColor( PlatynereisDataSource dataSource )
    {
        if ( dataSource.name.contains( Constants.EM_FILE_ID ) )
        {
            return Color.WHITE;
        }
        else if ( panels.size() <= colors.size() )
        {
            return colors.get( panels.size() - 1 );

        }
        else
        {
            return colors.get( 0 );
        }
    }

    public void addSource( String name )
    {
        PlatynereisDataSource source = dataSources.get( name );

        addSourceToViewer( source );

        addSourceToPanel( source );
    }

    private void addSourceToViewer( PlatynereisDataSource source )
    {
        if ( source.bdvSource == null )
        {
            switch ( Constants.BDV_XML_SUFFIX ) // TODO: makes no sense...
            {
                case ".tif":
                    Utils.loadAndShowSourceFromTiffFile( source, bdv  );
                    break;

                case ".xml":
                    if ( source.spimData == null )
                    {
                        source.spimData = Utils.openSpimData( source.file );
                    }
                    Utils.showSourceInBdv( source, bdv  );
                    break;

                default:
                    Utils.log( "Unsupported format: " + Constants.BDV_XML_SUFFIX );
            }
        }

        source.bdvSource.setActive( true );
        source.isActive = true;
        source.color = getColor( source );
        source.bdvSource.setColor(  asArgbType( source.color ) );
    }

    public void addSourceToPanel( PlatynereisDataSource dataSource )
    {

        if( ! panels.containsKey( dataSource.name ) )
        {

            int[] buttonDimensions = new int[]{ 40, 40 };

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            panel.add(Box.createHorizontalGlue());
            panel.setOpaque( true );
            dataSource.color = getColor( dataSource );
            panel.setBackground( dataSource.color );

            JLabel jLabel = new JLabel( dataSource.name );
            jLabel.setHorizontalAlignment( SwingConstants.CENTER );

            JButton color = new JButton( "C" );
            color.addActionListener( this );
            color.setPreferredSize( new Dimension( buttonDimensions[ 0 ],buttonDimensions[ 1 ] ) );
            color.setName( COLOR_ACTION + dataSource.name );

            JButton brightness = new JButton( "B" );
            brightness.addActionListener( this );
            brightness.setPreferredSize( new Dimension( buttonDimensions[ 0 ],buttonDimensions[ 1 ] ) );
            brightness.setName( BRIGHTNESS_ACTION + dataSource.name );

            JButton toggle = new JButton( "T" );
            toggle.addActionListener( this );
            toggle.setPreferredSize( new Dimension( buttonDimensions[ 0 ],buttonDimensions[ 1 ] ) );
            toggle.setName( TOGGLE_ACTION + dataSource.name );

            JButton remove = new JButton( "X" );
            remove.addActionListener( this );
            remove.setPreferredSize( new Dimension( buttonDimensions[ 0 ],buttonDimensions[ 1 ] ) );
            remove.setName( REMOVE_ACTION + dataSource.name );

            panel.add( jLabel );
            panel.add( color );
            panel.add( brightness );
            panel.add( toggle );
            panel.add( remove );

            //Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream("font.ttf"));
            //button.setFont( new Font.createFont( Font.TRUETYPE_FONT ) );

            add( panel );
            panels.put( dataSource.name, panel );

            refreshGui();

        }

    }


    private String padded( String string )
    {
        return string;
        //return String.format("%20s", string);
    }


    private ArrayList< String > getCurrentSourceNames()
    {
        return new ArrayList<>( panels.keySet() );
    }

    public void removeAllProSPrSources()
    {
        final ArrayList< String > names = getCurrentSourceNames();

        for ( String name : names )
        {
            if ( ! name.contains( Constants.EM_FILE_ID ) )
            {
                removeSource( name );
            }
        }
    }

    private void removeSource( String name )
    {
        mainCommand.hideDataSource( name );
        remove( panels.get( name ) );
        panels.remove( name );
        refreshGui();
    }

    private void refreshGui()
    {
        this.revalidate();
        this.repaint();
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {

        JButton button = (JButton) e.getSource();
        String name = button.getName().trim();

        if ( name.contains( COLOR_ACTION ))
        {
            String dataSourceName = name.replace( COLOR_ACTION, "" );
            changeColorViaUI( dataSourceName );
        }
        else if ( name.contains( BRIGHTNESS_ACTION ) )
        {
            String dataSourceName = name.replace( BRIGHTNESS_ACTION, "" );
            mainCommand.setBrightness( dataSourceName );
        }
        else if( name.contains( TOGGLE_ACTION ) )
        {
            String dataSourceName = name.replace( TOGGLE_ACTION, "" );
            toggleVisibility( dataSourceName );
        }
        else if( name.contains( REMOVE_ACTION ) )
        {
            String dataSourceName = name.replace( REMOVE_ACTION, "" );
            removeSource( dataSourceName );
        }
    }


    public void toggleVisibility( String dataSourceName )
    {
        boolean isActive = dataSources.get( dataSourceName ).isActive;
        dataSources.get( dataSourceName ).isActive = !isActive ;
        dataSources.get( dataSourceName ).bdvSource.setActive( !isActive );
    }

    private String getActionFromUI()
    {
        GenericDialog genericDialog = new GenericDialog( "Choose action " );

        genericDialog.addChoice( "Action", new String[]
                {
                        CHANGE_COLOR,
                        ADAPT_BRIGHTNESS,
                        REMOVE
                },
                CHANGE_COLOR);

        genericDialog.showDialog();

        if ( genericDialog.wasCanceled() ) return CANCELLED;

        return genericDialog.getNextChoice();
    }

    public void changeColorViaUI( String name )
    {
        Color color = JColorChooser.showDialog( null, "Select color for " + name, null );

        if ( color != null )
        {
            mainCommand.setDataSourceColor( name, color );
            panels.get( name ).setBackground( color );
        }

    }



}