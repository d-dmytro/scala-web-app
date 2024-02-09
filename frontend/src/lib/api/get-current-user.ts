import { isAxiosError } from "axios";
import { client } from "./client";

export const getCurrentUser = async () => {
  try {
    const res = await client.get("/me", { withCredentials: true });
    return res.data;
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 403) {
      // User is not authenticated.
      return null;
    }

    throw error;
  }
};
