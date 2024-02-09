import { isAxiosError } from "axios";
import { client } from "./client";

export const refreshAuth = async (userId: string): Promise<void> => {
  try {
    await client.post(
      "/refresh",
      { userId },
      {
        withCredentials: true,
      }
    );
  } catch (error) {
    if (isAxiosError(error) && error.response && error.response.status < 500) {
      return;
    }

    throw error;
  }
};
