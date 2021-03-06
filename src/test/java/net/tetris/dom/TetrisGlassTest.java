package net.tetris.dom;

import net.tetris.services.Plot;
import net.tetris.services.PlotColor;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;
import static net.tetris.dom.TestUtils.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TetrisGlassTest {

    public static final int CENTER_X = WIDTH / 2 - 1;
    public static final int CENTER_Y = HEIGHT - 1;
    private TetrisGlass glass;
    private TetrisFigure point;
    private TetrisFigure glassWidthFigure;
    private TetrisFigure line9Width;
    @Mock
    private GlassEventListener glassEventListener;
    @Captor
    private ArgumentCaptor<Integer> removedLines;
    @Captor
    private ArgumentCaptor<Figure> droppedFigure;

    @Before
    public void setUp() throws Exception {
        glass = new TetrisGlass(WIDTH, HEIGHT, glassEventListener);
        point = new TetrisFigure();
        glassWidthFigure = new TetrisFigure(0, 0, StringUtils.repeat("#", WIDTH));
        line9Width = new TetrisFigure(0, 0, StringUtils.repeat("#", WIDTH - 1));
    }

    @Test
    public void shouldAcceptWhenEmpty() {
        assertTrue(glass.accept(new TetrisFigure(), 1, 1));
    }

    @Test
    public void shouldRejectWhenAcceptingOnDroppedPlace() {
        glass.drop(new TetrisFigure(), 0, 0);

        assertFalse(glass.accept(new TetrisFigure(), 0, 0));
    }

    @Test
    public void shouldPerformDropWhenDropRequested() {
        glass.drop(new TetrisFigure(), 0, HEIGHT);

        assertFalse(glass.accept(new TetrisFigure(), 0, 0));
    }

    @Test
    public void shouldRejectWhenFigurePartlyOutsideFromLeft() {
        assertFalse(glass.accept(new TetrisFigure(1, 0, "##"), 0, HEIGHT));
    }

    @Test
    public void shouldRejectWhenFigurePartlyOutsideFromRight() {
        assertFalse(glass.accept(new TetrisFigure(0, 0, "##"), WIDTH, HEIGHT));
    }

    @Test
    public void shouldRejectWhenFigurePartlyOutsideBottom() {
        assertFalse(glass.accept(new TetrisFigure(0, 0, "#", "#"), WIDTH, 0));
    }

    @Test
    public void shouldRejectWhenFigurePartlyOverDroppedRow() {
        TetrisFigure figure = new TetrisFigure(1, 0, "##");

        glass.drop(figure, WIDTH / 2, HEIGHT);

        assertFalse(glass.accept(figure, WIDTH / 2 + 1, 0));
        assertFalse(glass.accept(figure, WIDTH / 2, 0));
        assertFalse(glass.accept(figure, WIDTH / 2 - 1, 0));
    }

    @Test
    public void shouldAcceptWhenFigureAboveDropped() {
        glass.drop(new TetrisFigure(1, 0, "##"), WIDTH / 2, HEIGHT);

        assertTrue(glass.accept(new TetrisFigure(1, 0, "##"), WIDTH / 2, 1));
    }

    @Test
    public void shouldRejectWhenFigurePartlyOverDroppedColumn() {
        TetrisFigure figure = new TetrisFigure(0, 1, "#", "#");

        glass.drop(figure, WIDTH / 2, HEIGHT);

        assertFalse(glass.accept(figure, WIDTH / 2, 0));
        assertFalse(glass.accept(figure, WIDTH / 2, 1));
        assertTrue(glass.accept(figure, WIDTH / 2, 2));
    }

    @Test
    public void shouldRejectWhenFigureOverlapDroppedColumnByBottomSize() {
        TetrisFigure figure = new TetrisFigure(0, 0, "#", "#");

        glass.drop(figure, WIDTH / 2, HEIGHT);

        assertFalse(glass.accept(figure, WIDTH / 2, 2));
    }

    @Test
    public void shouldRejectWhenFigureIsNotSymmetric() {
        TetrisFigure figure = new TetrisFigure(1, 0, "##", " #");

        glass.drop(figure, WIDTH / 2, HEIGHT);

        assertFalse(glass.accept(point, WIDTH / 2 - 1, 1));
    }

    @Test
    public void shouldRejectWhenFigureIsNotSymmetricPositive() {
        TetrisFigure figure = new TetrisFigure(1, 0, "##", " #");

        glass.drop(figure, WIDTH / 2, HEIGHT);

        assertTrue(glass.accept(figure, WIDTH / 2 + 1, 2));
    }

    @Test
    public void shouldRejectWhenFigureDroppedToFilledGlass() {
        glass.drop(point, 0, HEIGHT);

        glass.drop(point, 0, HEIGHT);

        assertFalse(glass.accept(point, 0, 1));
    }


    @Test
    public void shouldIgnoreWhenDropOutside() {
        glass.drop(new TetrisFigure(0, 0, "##"), -1, HEIGHT - 1);

        assertTrue(glass.accept(point, 0, 0));
    }

    @Test
    public void shouldIgnoreWhenOutOfBoundsLeft() {
        glass.drop(new TetrisFigure(1, 0, "##"), 0, HEIGHT - 1);

        assertTrue(glass.isEmpty());
    }

    @Test
    public void shouldIgnoreWhenOutOfBoundsRightPartially() {
        glass.drop(new TetrisFigure(0, 0, "##"), WIDTH - 1, HEIGHT - 1);

        assertTrue(glass.isEmpty());
    }

    @Test
    public void shouldIgnoreWhenOutOfBoundsBottom() {
        glass.drop(new TetrisFigure(0, 0, "#", "#"), 0, 0);

        assertTrue(glass.isEmpty());
    }

    @Test
    public void shouldRejectWhenPartiallyOutsideOnRight() {
        assertFalse(glass.accept(new TetrisFigure(0, 0, "##"), WIDTH - 1, 0));
    }

    @Test
    public void shouldReturnPlotCoordinateSimpleFigure() {
        glass.figureAt(point, 1, 1);

        Plot plot = glass.getCurrentFigurePlots().get(0);
        assertContainsPlot(1, 1, PlotColor.CYAN, plot);
    }

    @Test
    public void shouldReturnPlotCoordinateHorizontalFigure() {
        glass.figureAt(new TetrisFigure(1, 0, "###"), 1, 1);

        List<Plot> plots = glass.getCurrentFigurePlots();
        TestUtils.assertContainsPlot(1 - 1, 1, PlotColor.CYAN, plots);
        TestUtils.assertContainsPlot(1, 1, PlotColor.CYAN, plots);
        TestUtils.assertContainsPlot(1 + 1, 1, PlotColor.CYAN, plots);
    }

    @Test
    public void shouldReturnPlotCoordinateVerticalFigure() {
        glass.figureAt(new TetrisFigure(0, 1, "#", "#", "#"), 1, 3);

        List<Plot> plots = glass.getCurrentFigurePlots();
        TestUtils.assertContainsPlot(1, 3 + 1, PlotColor.CYAN, plots);
        TestUtils.assertContainsPlot(1, 3, PlotColor.CYAN, plots);
        TestUtils.assertContainsPlot(1, 3 - 1, PlotColor.CYAN, plots);
    }

    @Test
    public void shouldReturnPlotCoordinateAsymetricFigure() {
        glass.figureAt(new TetrisFigure(1, 0, " #"), 1, 0);

        List<Plot> plots = glass.getCurrentFigurePlots();
        assertEquals(1, plots.size());
        TestUtils.assertContainsPlot(1, 0, PlotColor.CYAN, plots);
    }

    @Test
    public void shouldReturnPlotCoordinateAsymetricFigure2() {
        glass.figureAt(new TetrisFigure(0, 0, " #"), 1, 0);

        List<Plot> plots = glass.getCurrentFigurePlots();
        assertEquals(1, plots.size());
        TestUtils.assertContainsPlot(1 + 1, 0, PlotColor.CYAN, plots);
    }

    @Test
    public void shouldReturnEmptyArrayWhenNoFigure() {
        assertTrue(glass.getCurrentFigurePlots().isEmpty());
    }

    @Test
    public void shouldReturnPlotOfDroppedFigure() {
        glass.drop(point, 0, HEIGHT);

        List<Plot> plots = glass.getDroppedPlots();
        TestUtils.assertContainsPlot(0, 0, PlotColor.CYAN, plots);
    }

    @Test
    public void shouldReturnPlotOfDroppedFigure2() {
        glass.drop(new TetrisFigure(1, 1, "##", "##"), 3, HEIGHT);

        List<Plot> plots = glass.getDroppedPlots();
        TestUtils.assertContainsPlot(3 - 1, 1, PlotColor.CYAN, plots);
        TestUtils.assertContainsPlot(3, 1, PlotColor.CYAN, plots);
        TestUtils.assertContainsPlot(3 - 1, 0, PlotColor.CYAN, plots);
        TestUtils.assertContainsPlot(3, 0, PlotColor.CYAN, plots);
    }


    @Test
    public void shouldEmptyWhenRequested() {
        glass.drop(point, 0, 0);

        glass.empty();

        assertTrue(glass.getDroppedPlots().isEmpty());
    }

    @Test
    public void shouldRemoveFilledLineAfterDrop() {
        glass.drop(glassWidthFigure, 0, 0);

        assertTrue(glass.isEmpty());
    }

    @Test
    public void shouldRemoveFilledLineWhenSeveralFilled() {
        TetrisFigure columnFigure = new TetrisFigure(0, 0, "#", "#");
        glass.drop(line9Width, 0, HEIGHT);
        glass.drop(line9Width, 0, HEIGHT);

        glass.drop(columnFigure, WIDTH - 1, HEIGHT);

        assertTrue(glass.isEmpty());
    }

    @Test
    public void shouldRemoveFilledWhenGarbageOnTop() {
        drop(line9Width, HEIGHT);

        glass.drop(point, WIDTH - 1, HEIGHT);

        assertTrue(glass.accept(glassWidthFigure, 0, HEIGHT - 1));
    }

    @Test
    public void shouldRemoveFilledLineWhenInMiddleOfGlass() {
        glass.drop(point, 0, HEIGHT);
        glass.drop(line9Width, 0, HEIGHT);

        glass.drop(new TetrisFigure(0, 0, "#", "#"), WIDTH - 1, HEIGHT);

        assertTrue(glass.accept(glassWidthFigure, 0, 1));
    }

    @Test
    public void shouldNotifyWhenLineRemoved() {
        glass.drop(glassWidthFigure, 0, HEIGHT);

        verify(glassEventListener).linesRemoved(removedLines.capture());
        assertEquals(1, removedLines.getValue().intValue());
    }

    @Test
    public void shouldNotifyWhenSeveralLinesRemoved() {
        glass.drop(line9Width, 0, HEIGHT);
        glass.drop(line9Width, 0, HEIGHT);

        glass.drop(new TetrisFigure(0, 0, "#", "#"), WIDTH - 1, HEIGHT);

        verify(glassEventListener).linesRemoved(removedLines.capture());
        assertEquals(2, removedLines.getValue().intValue());
    }

    @Test
    public void shouldNotifyScoreBoardWhenDropped() {
        glass.drop(point, 0, HEIGHT);

        verify(glassEventListener).figureDropped(droppedFigure.capture());
        assertSame(point, droppedFigure.getValue());
    }

    @Test
    public void shouldTriggerListenerWhenOverflow() {
        glass.empty();

        verify(glassEventListener).glassOverflown();
    }

    @Test
    public void shouldTriggerAllListenersWhenOverflow() {
        TetrisGlass glass = new TetrisGlass(WIDTH, HEIGHT, glassEventListener, glassEventListener);

        glass.empty();

        verify(glassEventListener, times(2)).glassOverflown();
    }

    @Test
    public void shouldNotifyAllListenersScoreBoardWhenDropped() {
        TetrisGlass glass = new TetrisGlass(WIDTH, HEIGHT, glassEventListener, glassEventListener);

        glass.drop(point, 0, HEIGHT);

        verify(glassEventListener, times(2)).figureDropped(droppedFigure.capture());
    }

    @Test
    public void shouldNotifyAllListenersScoreBoardWhenLinesRemoved() {
        TetrisGlass glass = new TetrisGlass(WIDTH, HEIGHT, glassEventListener, glassEventListener);

        glass.drop(glassWidthFigure, 0, HEIGHT);

        verify(glassEventListener, times(2)).figureDropped(droppedFigure.capture());
    }

    @Test
    public void shouldQuietWhenDropOutOfBounds() {
        glass.drop(createVerticalFigure(HEIGHT), WIDTH / 2, HEIGHT);

        try {
            glass.drop(Figure.Type.I.createNewFigure(), WIDTH / 2, HEIGHT);
            verify(glassEventListener, times(1)).figureDropped(Matchers.<Figure>any());
        } catch (Exception e) {
            fail("Was exception: " + e);
        }
    }

    @Test
    public void shouldRemainWhenDropped_FoundInIntegrationTesting() {
        TetrisFigure twoRowsFigure = new TetrisFigure(0, 0, "#", "#");
        glass.drop(twoRowsFigure, WIDTH / 2, HEIGHT);

        glass.drop(twoRowsFigure, WIDTH / 2, HEIGHT);

        assertFalse(glass.accept(point, WIDTH / 2, 2));
        assertFalse(glass.accept(point, WIDTH / 2, 3));
    }

    @Test
    public void shouldBeOccupiedWhenLongFigureDroppedOnTopOfGlass() {
        glass.drop(createVerticalFigure(HEIGHT - 2), CENTER_X, CENTER_Y);
        Figure figure = Figure.Type.J.createNewFigure();
        glass.drop(figure, CENTER_X, CENTER_Y);

        assertFalse(glass.accept(figure, CENTER_X, CENTER_Y));
    }

    private TetrisFigure createVerticalFigure(int height) {
        String[] verticalLine = new String[height];
        Arrays.fill(verticalLine, "#");
        return new TetrisFigure(0, 0, verticalLine);
    }

    private void drop(TetrisFigure lineFigure, int times) {
        for (int i = 0; i < times; i++) {
            glass.drop(lineFigure, 0, HEIGHT);
        }
    }


    private void assertContainsPlot(final int x, final int y, final PlotColor color, Plot... plots) {
        TestUtils.assertContainsPlot(x, y, color, Arrays.asList(plots));
    }
}
