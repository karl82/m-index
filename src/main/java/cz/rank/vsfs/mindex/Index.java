/*
 * Copyright © 2012 Karel Rr without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *  Neither the name of Karel Rank nor the names of its contributors may be used to
 *   endorse or promote products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cz.rank.vsfs.mindex;

import net.jcip.annotations.Immutable;
import org.apache.commons.math3.util.FastMath;

/**
 * Index for cluster
 *
 * @author Karel Rank
 */
@Immutable
public class Index {
    private final int index;
    private final int maxLevel;
    private final int level;

    public Index(int index, int maxLevel) {
        this.index = index;
        this.maxLevel = maxLevel;
        level = 0;
    }

    private Index(int index, int maxLevel, int level) {
        this.index = index;
        this.maxLevel = maxLevel;
        this.level = level;
    }

    public int getIndex() {
        return index;
    }

    public Index addLevel(int index) {
        final int calculatedIndex = (int) (this.index * FastMath.pow(maxLevel, level + 1) + index);
        return new Index(calculatedIndex, maxLevel, level + 1);
    }

    @Override
    public String toString() {
        return "Index{" +
                "index=" + index +
                ", maxLevel=" + maxLevel +
                ", level=" + level +
                '}';
    }
}
