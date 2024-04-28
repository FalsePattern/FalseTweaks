/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.falsetweaks.modules.occlusion;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class InterruptableSorter<T> {
    private final Comparator<T> comp;

    public void interruptableSort(T[] A, int low, int high) throws InterruptedException {
        quickSort(A, low, high);
    }

    private void quickSort(T[] A, int low, int high) throws InterruptedException {
        checkInterrupt();
        if (low < high + 1) {
            int p = partition(A, low, high);
            quickSort(A, low, p - 1);
            quickSort(A, p + 1, high);
        }
    }

    private void swap(T[] A, int index1, int index2) {
        val temp = A[index1];
        A[index1] = A[index2];
        A[index2] = temp;
    }

    private int getPivot(int low, int high) {
        return (low + high) / 2;
    }

    private int partition(T[] A, int low, int high) {
        swap(A, low, getPivot(low, high));
        int border = low + 1;
        for (int i = border; i <= high; i++) {
            if (comp.compare(A[i], A[low]) < 0) {
                swap(A, i, border++);
            }
        }
        swap(A, low, border - 1);
        return border - 1;
    }


    public void interruptableSort(List<T> A) throws InterruptedException {
        quickSort(A, 0, A.size() - 1);
    }

    private void quickSort(List<T> A, int low, int high) throws InterruptedException {
        checkInterrupt();
        if (low < high + 1) {
            int p = partition(A, low, high);
            quickSort(A, low, p - 1);
            quickSort(A, p + 1, high);
        }
    }

    private void checkInterrupt() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    private void swap(List<T> A, int index1, int index2) {
        val temp = A.get(index1);
        A.set(index1, A.get(index2));
        A.set(index2, temp);
    }

    private int partition(List<T> A, int low, int high) {
        swap(A, low, getPivot(low, high));
        int border = low + 1;
        for (int i = border; i <= high; i++) {
            if (comp.compare(A.get(i), A.get(low)) < 0) {
                swap(A, i, border++);
            }
        }
        swap(A, low, border - 1);
        return border - 1;
    }
}
