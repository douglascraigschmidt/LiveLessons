package edu.vuum.mocca;

import android.util.SparseArray;

/**
 * @class ButtonStrategyMapper
 *
 * @brief Maps buttons (represented via their resource ids) to
 *        ButtonStrategy implementations.
 */
public class ButtonStrategyMapper {
    private SparseArray<ButtonStrategy> mButtonStrategyArray =
        new SparseArray<ButtonStrategy>();
            
    public ButtonStrategyMapper(int[] buttonIds,
                                ButtonStrategy[] buttonStrategys) {
        // Map buttons pushed by the user to the requested type of
        // ButtonStrategy.
        for (int i = 0; i < buttonIds.length; ++i)
            mButtonStrategyArray.put(buttonIds[i],
                                     buttonStrategys[i]);
    }

    /**
     * Factory method that returns the request ButtonStrategy
     * implementation.
     */
    public ButtonStrategy getButtonStrategy(int buttonId) {
        // Return the designated ButtonStrategy.
        return mButtonStrategyArray.get(buttonId);
    }
}

