/**
 * 用户类别
 */
export type UserType = {
    id?: number,
    userName?: string;
    loginName?: string;
    avatarUrl?: string;
    gender?: number;
    tags: string[];
    profile?: string;
    phone?: string;
    email?: string;
    status?: number;
    userRole?: number;
    createTime?: Date;
};