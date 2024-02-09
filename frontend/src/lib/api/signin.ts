import { client } from "./client";
import { User } from "./types";

interface SigninParams {
  email: string;
  password: string;
}

export const signin = async (params: SigninParams): Promise<User> => {
  const res = await client.post<User>("/signin", params, {
    withCredentials: true,
  });
  return res.data;
};
