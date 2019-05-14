package com.example.custom;

/**
 * Created by 许格.
 * Date on 2019/5/14.
 * dec:
 */
public interface AccountControlListener {

    /**
     * 点击了某个条目
     *
     * @param account 所选的账户名称
     */
    void clickItem(String account);

    /**
     * 删除了某个条目
     *
     * @param position 删除的条目索引
     */
    void deleteItem(int position);
}
