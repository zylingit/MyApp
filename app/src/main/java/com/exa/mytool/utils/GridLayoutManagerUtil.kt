package com.aam.mida.mida_yk.utils

/**
 *
 * @Description RecyclerView GridLayoutManager相关工具方法
 * @Author zechao.zhang
 * @CreateTime 2024/10/10
 */
object GridLayoutManagerUtil {

    /**
     * 计算均分需要的offset。
     * GridLayoutManager平分原理：
     * 步骤1. 平分RecyclerView的宽度，得到每个网格的宽度gridWidth = parentWidth / gridSize
     * 步骤2. 减去每个item左右间距，得到Item的宽度：childWidth = gridWidth - outRect.left - outRect.right
     *
     * 假设：
     * 左右两边距离parent布局的距离分别为：leftRightPadding；
     * Item之间的间隔为：spacingH；
     * 列数为：columnCount；
     * 第n个item左边的边距 定为 L(n), 右边的边距定为R(n)， 将他们的和定为p，则有：
     * （1）L(n) + R(n) = p
     * （2）R(n) + L(n+1) = spacingH
     *
     * 对于从0 <= n < gridSize，有如下公式：
     * L(0) + R(0) = p
     * L(1) + R(1) = p
     * ...
     * L(columnCount-1) + R(columnCount-1) = p
     *
     * 从纵向部分将等式左右两边想加，可得：
     * L(0) + (columnCount - 1) * h + R(columnCount -1 ) = columnCount * p
     *
     * 又由于网格两边都为leftRightPadding，即L(0)和R(columnCount -1 )为leftRightPadding，结合公式（2），可以算出p的值为：
     * p = (2 * leftRightPadding + (columnCount - 1) * spacingH) / columnCount
     *
     * 再仔细发现公式（1）和（2）左边都有R(n)，我们通过减法将他消除掉消除掉，即②-①，就剩下:
     * L(n+1) - L(n) = spacingH - p
     *
     * 这个式子明显是一个等差数列，等差数列是有公式的，可以直接得出一下结论：
     * L(n) = L(0) + n * (spacingH - p)，
     * 由于L(0)为leftRightPadding，可以推断出：
     * L(n) = leftRightPadding + n * (spacingH - p)
     *
     * 由公式（1）可得：
     * R(n) = p - L(n)。
     *
     * @param leftRightPadding 第0列和最后一列分别距离Parent布局的边距
     * @param spacingH Item之间的距离
     * @param columnCount 列数
     * @return 返回每个Item的左右offset数值，Pair(leftOffset, rightOffset)
     */
    fun getItemOffsetArray(leftRightPadding: Float,
                                   spacingH: Float, columnCount: Int): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()

        for (index in 0 until columnCount) {
            // p为每个Item都需要减去的间距
            val  p = (2 * leftRightPadding + (columnCount - 1) * spacingH) * 1f / columnCount
            val left = leftRightPadding + index * (spacingH - p)
            val right = p - left
            result.add(Pair(Math.round(left), Math.round(right)))
        }

        return result
    }
}