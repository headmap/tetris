package net.tetris.dom;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author serhiy.zelenin
 */
@RunWith(MockitoJUnitRunner.class)
public class TetrisGameTest {
    public static final int CENTER_X = 10/2 - 1;
    public static final int TOP_Y = 20;
    public static final int HEIGHT = 20;
    @Mock FigureQueue queue;
    @Mock Glass glass;
    @Captor ArgumentCaptor<Integer> xCaptor;
    @Captor ArgumentCaptor<Integer> yCaptor;
    @Captor ArgumentCaptor<Figure> figureCaptor;

    //field value will be initialized in GameSetupRule
    private TetrisGame game;

    @Rule public GameSetupRule gameSetup = new GameSetupRule();
    private TetrisFigure letterIFigure;


    @Before
    public void setUp() throws Exception {
        letterIFigure = new TetrisFigure(0, 1, Figure.Type.I, "#", "#", "#", "#");
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldBeInInitialPositionWhenGameStarts() {
        assertCoordinates(CENTER_X, TOP_Y);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldBeMovedLeftWhenAsked() {
        game.moveLeft(2);
        game.nextStep();

        assertCoordinates(CENTER_X - 2, TOP_Y - 1);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldChangePositionWhenOnlyNextStep(){
        game.moveLeft(2);

        assertCoordinates(CENTER_X, TOP_Y);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldBeMovedRightWhenAsked() {
        game.moveRight(2);
        game.nextStep();

        assertCoordinates(CENTER_X + 2, TOP_Y - 1);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldNotChangeCurrentFigureWhenNextStep(){
        game.nextStep();

        captureFigureAtValues();
        List<Figure> allFigures = figureCaptor.getAllValues();
        assertSame(allFigures.get(0), allFigures.get(1));
    } 

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldNotMoveOutWhenLeftSide(){
        game.moveLeft(CENTER_X + 1);
        game.nextStep();

        assertCoordinates(0, TOP_Y - 1);
    }


    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldNotMoveOutWhenRightSide(){
        game.moveRight(CENTER_X + 2);
        game.nextStep();

        assertCoordinates(9, TOP_Y - 1);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties(left = 1)})
    public void shouldIncludeFigureSizeWhenMoveLeft() {
        game.moveLeft(CENTER_X + 1);
        game.nextStep();

        assertCoordinates(1, HEIGHT - 1);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties(right = 1)})
    public void shouldIncludeFigureSizeWhenMoveRight() {
        game.moveRight(CENTER_X + 1);
        game.nextStep();

        assertCoordinates(9 - 1, HEIGHT - 1);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties(top = 1)})
    public void shouldAdjustFigurePositionWhenFigureHasTopCells(){
        assertCoordinates(CENTER_X, HEIGHT - 1);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties(bottom = 1), @FigureProperties(bottom = 2)})
    public void shouldTakeNextFigureWhenCurrentIsDropped() {
        game.drop();
        game.nextStep();
        game.nextStep();

        assertCoordinates(CENTER_X, HEIGHT);
        assertEquals(2, figureCaptor.getValue().getBottom());
    }
    
    @Test
    @GivenFiguresInQueue({@FigureProperties(bottom = HEIGHT), @FigureProperties(bottom = 1)})
    public void shouldGameOverWhenGlassOverflown() {
        glassToRejectFigure();
        game.nextStep();

        assertCoordinates(CENTER_X, HEIGHT);
        assertGameOver();
    }

    private void verifyDroppedAt(int x, int y) {
        verify(glass).drop(Matchers.<Figure>anyObject(), eq(x), eq(y));
    }

    private void assertGameOver() {
        verify(glass).empty();
    }

    private void glassToRejectFigure() {
        glassToAccept(false);
    }

    private void glassToAccept(boolean accept) {
        when(glass.accept(Matchers.<Figure>anyObject(), anyInt(), anyInt())).thenReturn(accept);
    }

    private void glassToAcceptFigure() {
        glassToAccept(true);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldBeAtSameCoordinatesWhenDropRequested(){
        game.drop();
        game.moveLeft(1);
        game.nextStep();
        
        assertCoordinates(CENTER_X, HEIGHT);
    } 

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldMoveLeftWhenAcceptOnly(){
        rejectWhenCoordinates(CENTER_X - 1, HEIGHT);
        acceptWhenCoordinates(CENTER_X, HEIGHT - 1);

        game.moveLeft(1);
        game.nextStep();

        assertCoordinates(CENTER_X, HEIGHT - 1);
    }

    private OngoingStubbing<Boolean> acceptWhenCoordinates(int x, int y) {
        return when(glass.accept(Matchers.<Figure>anyObject(), eq(x), eq(y))).thenReturn(true);
    }

    private OngoingStubbing<Boolean> rejectWhenCoordinates(int x, int y) {
        return when(glass.accept(Matchers.<Figure>anyObject(), eq(x), eq(y))).thenReturn(false);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldMoveRightWhenAcceptOnly() {
        rejectWhenCoordinates(CENTER_X + 1, HEIGHT);
        acceptWhenCoordinates(CENTER_X, HEIGHT - 1);

        game.moveRight(1);
        game.nextStep();

        assertCoordinates(CENTER_X, HEIGHT - 1);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldStayAtCurrentPositionWhenRejected(){
        game.nextStep();
        glassToRejectFigure();

        game.nextStep();

        assertCoordinates(CENTER_X, TOP_Y - 1);
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties, @FigureProperties(left = 1)})
    public void shouldTakeNextFigureWhenRejectedAfterNextStep(){
        acceptWhenCoordinates(CENTER_X, HEIGHT);
        rejectWhenCoordinates(CENTER_X, HEIGHT - 1);
        game.nextStep();
        game.nextStep();

        game.nextStep();

        captureFigureAtValues();
        assertThat(yCaptor.getAllValues()).isEqualTo(Arrays.asList(HEIGHT, HEIGHT));
        assertEquals(1, figureCaptor.getValue().getLeft());
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties, @FigureProperties(left = 1)})
    public void shouldOverflowWhenFigureRejectedAtFirstStep(){
        rejectWhenCoordinates(CENTER_X, HEIGHT);

        game.nextStep();

        assertCoordinates(CENTER_X, HEIGHT);
        assertGameOver();
    }

    @Test
    @GivenFiguresInQueue({@FigureProperties})
    public void shouldPerformDropWhenRejected(){
        acceptWhenCoordinates(CENTER_X, HEIGHT);
        rejectWhenCoordinates(CENTER_X, HEIGHT - 1);

        game.nextStep();

        verifyDroppedAt(CENTER_X, HEIGHT);
    }

    @Test
    public void shouldRotateOnce(){
        TetrisGame game = createGameWithOneFigureInQueue(letterIFigure);
        glassToAcceptFigure();
        game.nextStep();

        game.rotate(1);

        captureFigureAtValues();
        Figure capturedFigure = figureCaptor.getValue();
        assertThat(capturedFigure.getRowCodes()).isEqualTo(new int[]{0b1111});
    }

    @Test
    public void shouldIgnoreRotationWhenNotAccepted(){
        TetrisGame game = createGameWithOneFigureInQueue(letterIFigure);
        glassToAcceptFigure();
        game.nextStep();

        glassToRejectFigure();
        game.rotate(1);

        captureFigureAtValues();
        Figure capturedFigure = figureCaptor.getValue();
        assertThat(capturedFigure.getRowCodes()).isEqualTo(new int[]{0b1, 0b1, 0b1, 0b1});
    }


    private TetrisGame createGameWithOneFigureInQueue(final TetrisFigure figure) {
        return new TetrisGame(new FigureQueue() {
                @Override
                public Figure next() {
                    return figure;
                }
            }, glass);
    }


    private void assertCoordinates(int x, int y) {
        captureFigureAtValues();
        assertEquals(x, xCaptor.getValue().intValue());
        assertEquals(y, yCaptor.getValue().intValue());
    }

    private void captureFigureAtValues() {
        verify(glass, atLeastOnce()).figureAt(figureCaptor.capture(), xCaptor.capture(), yCaptor.capture());
    }
}
